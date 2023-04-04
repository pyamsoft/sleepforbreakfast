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
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
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

    val note =
        """
Automatically created from Notification

${auto.notificationMatchText}
"""
            .trimIndent()

    val transaction =
        DbTransaction.create(clock, DbTransaction.Id.EMPTY)
            // Tie to this automatic
            .automaticId(auto.id)
            .automaticCreatedDate(LocalDate.now(clock))
            .replaceCategories(auto.categories)
            .amountInCents(auto.notificationAmountInCents)
            .type(auto.notificationType)
            .name(auto.notificationTitle)
            .date(getNotificationPostTime(auto))
            .note(note)

    return when (val result = transactionInsertDao.insert(transaction)) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(result.error, "Failed to create transaction: $auto -> $transaction")
        false
      }
      is DbInsert.InsertResult.Insert -> {
        Timber.d("Create auto transaction $auto -> $transaction")
        true
      }
      is DbInsert.InsertResult.Update -> {
        // This should not happen
        Timber.d("Update auto transaction $auto -> $transaction")
        true
      }
    }
  }

  private suspend fun markConsumed(automatic: DbAutomatic) {
    Timber.d("Created new transaction from auto, consume!")

    when (val result = automaticInsertDao.insert(automatic.consume())) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(result.error, "Failed to consume automatic: $automatic")
      }
      is DbInsert.InsertResult.Insert -> {
        // This should not happen
        Timber.d("Marked NEW automatic consumed: $automatic")
      }
      is DbInsert.InsertResult.Update -> {
        Timber.d("Marked automatic consumed: $automatic")
      }
    }
  }

  suspend fun process(automatic: DbAutomatic) {
    // If its already used, skip it
    if (automatic.used) {
      return
    }

    val created = createTransactionFromTemplate(automatic)
    if (created) {
      markConsumed(automatic)
    }
  }
}
