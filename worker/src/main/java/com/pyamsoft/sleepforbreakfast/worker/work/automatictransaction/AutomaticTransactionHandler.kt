package com.pyamsoft.sleepforbreakfast.worker.work.automatictransaction

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import java.time.Clock
import java.time.Instant
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

Package: ${auto.notificationPackageName}
ID: ${auto.notificationId}
Key: ${auto.notificationKey}
Group: ${auto.notificationGroup}
"""
            .trimIndent()

    val transaction =
        DbTransaction.create(clock, DbTransaction.Id.EMPTY)
            // Tie to this automatic
            .automaticId(auto.id)
            .clearCategories()
            .removeSourceId()
            .amountInCents(auto.notificationAmountInCents)
            .type(auto.notificationType)
            .date(getNotificationPostTime(auto))
            .name(auto.notificationTitle)
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

  suspend fun process(automatic: DbAutomatic) {
    // If its already used, skip it
    if (automatic.used) {
      return
    }

    val created = createTransactionFromTemplate(automatic)
    if (created) {
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
  }
}
