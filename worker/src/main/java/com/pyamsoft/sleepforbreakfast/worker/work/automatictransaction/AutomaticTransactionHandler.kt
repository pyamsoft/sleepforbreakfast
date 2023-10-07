/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.*
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

  private fun getNotificationPostTime(auto: DbAutomatic): LocalDateTime {
    val epoch = Instant.ofEpochMilli(auto.notificationPostTime)
    return LocalDateTime.ofInstant(epoch, TimeZone.getDefault().toZoneId())
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
            .date(getNotificationPostTime(auto))
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
  }
}
