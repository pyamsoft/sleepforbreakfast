/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.worker.work.automatictransaction

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.replaceCategories
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class AutomaticTransactionHandler
@Inject
internal constructor(
    private val automaticInsertDao: AutomaticInsertDao,
    private val transactionInsertDao: TransactionInsertDao,
    private val clock: Clock,
) {

  private val chaseTransactionDateFormatter by lazy {
    // Oct 7, 2023 at 1:23 AM ET
    DateTimeFormatter.ofPattern(CHASE_DATE_PATTERN)
  }

  @CheckResult
  private fun getAutomaticDate(auto: DbAutomatic): LocalDateTime? {
    val date = auto.notificationOptionalDate
    if (date.isBlank()) {
      return null
    }

    // Attempt to fix Chase's bad time zone
    // ET is not a time zone, it's either EST or EDT
    // figure out based on whether we are in DST right now or not
    val len = date.length
    val badTZWithSpace = date.substring(len - 3, len)
    // ET or PT or MT or CT or whatever-T
    if (badTZWithSpace.matches("\\s.T".toRegex())) {
      val goodTZ =
          if (isDst()) {
            "${badTZWithSpace[1]}D${badTZWithSpace[2]}"
          } else {
            "${badTZWithSpace[1]}S${badTZWithSpace[2]}"
          }
      val dateString = date.substring(0, len - 3)

      return try {
        Timber.d {
          "Parse timestamp into Chase Transaction Date: ${mapOf(
            "pattern" to CHASE_DATE_PATTERN,
            "raw" to date,
            "dateString" to dateString,
          )}"
        }

        // Just the date locally
        LocalDateTime.parse(dateString, chaseTransactionDateFormatter)
            .atZone(ZoneId.of(goodTZ, KNOWN_SHORT_IDS))
            .withZoneSameInstant(ZoneId.systemDefault())
            .toLocalDateTime()
      } catch (e: Throwable) {
        Timber.e(e) {
          "Failed to parse timestamp into Chase Transaction Date: ${mapOf(
            "pattern" to CHASE_DATE_PATTERN,
            "raw" to date,
            "dateString" to dateString,
          )}"
        }
        null
      }
    }

    return null
  }

  @CheckResult
  private fun getNotificationPostTime(auto: DbAutomatic): LocalDateTime {
    val epoch = Instant.ofEpochMilli(auto.notificationPostTime)
    return LocalDateTime.ofInstant(epoch, ZoneId.systemDefault())
  }

  @CheckResult
  private suspend fun createTransactionFromTemplate(auto: DbAutomatic): Boolean {

    val note = buildString {
      appendLine("Automatically created from Notification")

      appendLine()
      appendLine(auto.notificationMatchText)

      if (auto.notificationOptionalAccount.isNotBlank()) {
        appendLine()
        appendLine("Account: ${auto.notificationOptionalAccount}")
      }

      if (auto.notificationOptionalDate.isNotBlank()) {
        appendLine()
        appendLine("Date: ${auto.notificationOptionalDate}")
      }

      if (auto.notificationOptionalMerchant.isNotBlank()) {
        appendLine()
        appendLine("Merchant: ${auto.notificationOptionalMerchant}")
      }

      if (auto.notificationOptionalDescription.isNotBlank()) {
        appendLine()
        appendLine("Description: ${auto.notificationOptionalDescription}")
      }
    }

    val transaction =
        DbTransaction.create(clock, DbTransaction.Id.EMPTY)
            // Tie to this automatic
            .automaticId(auto.id)
            .automaticCreatedDate(LocalDate.now(clock))
            .replaceCategories(auto.categories)
            .amountInCents(auto.notificationAmountInCents)
            .type(auto.notificationType)
            .name(auto.notificationOptionalMerchant.ifBlank { auto.notificationTitle })
            .date(getAutomaticDate(auto) ?: getNotificationPostTime(auto))
            .note(note)

    return when (val result = transactionInsertDao.insert(transaction)) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(result.error) { "Failed to create transaction: $auto -> $transaction" }
        false
      }
      is DbInsert.InsertResult.Insert -> {
        Timber.d { "Create auto transaction $auto -> $transaction" }
        true
      }
      is DbInsert.InsertResult.Update -> {
        // This should not happen
        Timber.d { "Update auto transaction $auto -> $transaction" }
        true
      }
    }
  }

  private suspend fun markConsumed(automatic: DbAutomatic) {
    Timber.d { "Created new transaction from auto, consume!" }

    when (val result = automaticInsertDao.insert(automatic.consume())) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(result.error) { "Failed to consume automatic: $automatic" }
      }
      is DbInsert.InsertResult.Insert -> {
        // This should not happen
        Timber.d { "Marked NEW automatic consumed: $automatic" }
      }
      is DbInsert.InsertResult.Update -> {
        Timber.d { "Marked automatic consumed: $automatic" }
      }
    }
  }

  suspend fun process(automatic: DbAutomatic) =
      withContext(context = Dispatchers.Default) {
        // If its already used, skip it
        if (automatic.used) {
          return@withContext
        }

        GLOBAL_LOCK.withLock {
          val created = createTransactionFromTemplate(automatic)
          if (created) {
            markConsumed(automatic)
          }
        }
      }

  companion object {

    /**
     * the global lock prevents multiple callers from running this handler at the same time as it
     * could cause duplicates in the DB if operations are close enough
     */
    private val GLOBAL_LOCK = Mutex()

    private const val CHASE_DATE_PATTERN = "MMM d, yyyy 'at' h:mm a"

    private val localZoneRules by lazy { ZoneId.systemDefault().rules }

    private val KNOWN_SHORT_IDS by lazy {
      val base = ZoneId.SHORT_IDS
      base +
          mapOf(
              "ADT" to "-03:00",
              "EDT" to "-04:00",
              "CDT" to "-04:00",
              "HDT" to "-9:00",
              "MDT" to "-06:00",
              "PDT" to "-07:00",
          )
    }

    @CheckResult
    private fun isDst(): Boolean {
      return localZoneRules.isDaylightSavings(Instant.now())
    }
  }
}
