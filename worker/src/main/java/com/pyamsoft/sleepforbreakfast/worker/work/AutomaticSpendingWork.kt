package com.pyamsoft.sleepforbreakfast.worker.work

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.worker.BgWorker
import java.time.Clock
import java.time.Instant
import java.time.LocalDateTime
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class AutomaticSpendingWork
@Inject
internal constructor(
    private val automaticQueryDao: AutomaticQueryDao,
    private val automaticInsertDao: AutomaticInsertDao,
    private val transactionInsertDao: TransactionInsertDao,
    private val clock: Clock,
) : BgWorker {

  private fun getNotificationPostTime(auto: DbAutomatic): LocalDateTime {
    val epoch = Instant.ofEpochSecond(auto.notificationPostTime)
    return LocalDateTime.ofInstant(epoch, TimeZone.getDefault().toZoneId())
  }

  @CheckResult
  private suspend fun createTransactionFromTemplate(auto: DbAutomatic): Boolean {
    val transaction =
        DbTransaction.create(clock, DbTransaction.Id.EMPTY)
            // Tie to this automatic
            .automaticId(auto.id)
            .clearCategories()
            .removeSourceId()
            .amountInCents(auto.notificationAmountInCents)
            .date(getNotificationPostTime(auto))
            .name(auto.notificationTitle)
            .note(
                """
Automatically created from Notification

${auto.notificationMatchText}

Package: ${auto.notificationPackageName}
ID: ${auto.notificationId}
Key: ${auto.notificationKey}
Group: ${auto.notificationGroup}
"""
                    .trimIndent())
            // Assume this is a spend
            .type(DbTransaction.Type.SPEND)

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

  private suspend fun processJobs() {
    val unconsumed = automaticQueryDao.queryUnused()

    for (auto in unconsumed) {
      // Maybe I suck at SQL
      if (auto.used) {
        continue
      }

      val created = createTransactionFromTemplate(auto)
      if (created) {
        Timber.d("Created new transaction from auto, consume!")

        when (val result = automaticInsertDao.insert(auto.consume())) {
          is DbInsert.InsertResult.Fail -> {
            Timber.e(result.error, "Failed to consume automatic: $auto")
          }
          is DbInsert.InsertResult.Insert -> {
            // This should not happen
            Timber.d("Marked NEW automatic consumed: $auto")
          }
          is DbInsert.InsertResult.Update -> {
            Timber.d("Marked automatic consumed: $auto")
          }
        }
      }
    }
  }

  override suspend fun work(): BgWorker.WorkResult =
      withContext(context = Dispatchers.IO) {
        try {
          processJobs()
          return@withContext BgWorker.WorkResult.Success
        } catch (e: Throwable) {
          if (e is CancellationException) {
            Timber.w("Job cancelled during processing")
            return@withContext BgWorker.WorkResult.Cancelled
          } else {
            Timber.e(e, "Error during processing of unconsumed automatics")
            return@withContext BgWorker.WorkResult.Failed(e)
          }
        }
      }
}
