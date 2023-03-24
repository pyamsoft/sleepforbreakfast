package com.pyamsoft.sleepforbreakfast.spending

import android.os.Bundle
import android.service.notification.StatusBarNotification
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.automatic.queryByAutomaticNotification
import com.pyamsoft.sleepforbreakfast.spending.automatic.AutomaticManager
import com.pyamsoft.sleepforbreakfast.worker.WorkJobType
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
internal class SpendingTrackerHandlerImpl
@Inject
internal constructor(
    private val manager: AutomaticManager,
    private val automaticQueryDao: AutomaticQueryDao,
    private val automaticInsertDao: AutomaticInsertDao,
    private val workerQueue: WorkerQueue,
    private val clock: Clock,
) : SpendingTrackerHandler {

  private suspend fun handleProcessUnusedAutomatic(automatic: DbAutomatic) {
    val job = WorkJobType.ONESHOT_AUTOMATIC_TRANSACTION
    Timber.d("Enqueue job for processing $automatic: $job")
    workerQueue.enqueue(job)
  }

  override suspend fun processNotification(
      sbn: StatusBarNotification,
      extras: Bundle,
  ) {
    val automaticPayment = manager.extractPayment(sbn.packageName, extras) ?: return

    val automatic =
        DbAutomatic.create(clock)
            .notificationId(sbn.id)
            .notificationKey(sbn.key)
            .notificationGroup(sbn.groupKey)
            .notificationPackageName(sbn.packageName)
            .notificationPostTime(sbn.postTime)
            .notificationTitle(automaticPayment.title)
            .notificationMatchText(automaticPayment.text)
            .notificationAmountInCents(automaticPayment.amount)
            .notificationType(automaticPayment.type)

    when (val existing = automaticQueryDao.queryByAutomaticNotification(automatic)) {
      is Maybe.Data -> {
        Timber.w(
            "Found existing automatic notification matching parameters: ${mapOf(
                  "NEW" to automatic,
                  "EXISTING" to existing,
              )}")
      }
      is Maybe.None -> {
        when (val result = automaticInsertDao.insert(automatic)) {
          is DbInsert.InsertResult.Fail -> {
            Timber.e(result.error, "Failed to insert automatic $automatic")
          }
          is DbInsert.InsertResult.Update -> {
            Timber.d("Update existing automatic: $automatic")
          }
          is DbInsert.InsertResult.Insert -> {
            Timber.d("Inserted automatic: $automatic")
            handleProcessUnusedAutomatic(automatic)
          }
        }
      }
    }
  }
}
