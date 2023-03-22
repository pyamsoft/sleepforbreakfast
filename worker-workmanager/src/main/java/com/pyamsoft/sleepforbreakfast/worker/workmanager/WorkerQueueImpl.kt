package com.pyamsoft.sleepforbreakfast.worker.workmanager

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.pyamsoft.sleepforbreakfast.worker.WorkJobType
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import com.pyamsoft.sleepforbreakfast.worker.workmanager.workers.AutomaticSpendingWorker
import com.pyamsoft.sleepforbreakfast.worker.workmanager.workers.RepeatCreateTransactionWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class WorkerQueueImpl
@Inject
internal constructor(
    private val context: Context,
) : WorkerQueue {

  override suspend fun enqueue(type: WorkJobType) =
      withContext(context = Dispatchers.IO) {
        val builder: WorkRequest.Builder<*, *> =
            when (type) {
              WorkJobType.ONESHOT_AUTOMATIC_TRANSACTION ->
                  OneTimeWorkRequestBuilder<AutomaticSpendingWorker>()
              WorkJobType.REPEAT_CREATE_TRANSACTIONS ->
                  PeriodicWorkRequestBuilder<RepeatCreateTransactionWorker>(
                      // Repeat once a day
                      1L,
                      TimeUnit.DAYS,
                  )
              WorkJobType.ONESHOT_CREATE_TRANSACTIONS ->
                  OneTimeWorkRequestBuilder<RepeatCreateTransactionWorker>()
            }

        val work = builder.addTag(type.name).build()
        Timber.d("Enqueue work: $type")

        // Resolve the WorkManager instance
        WorkManager.getInstance(context).enqueue(work)

        // No return
        return@withContext
      }

  override suspend fun cancel(type: WorkJobType) =
      withContext(context = Dispatchers.IO) {

        // Resolve the WorkManager instance
        Timber.d("Cancel work by tag: $type")
        WorkManager.getInstance(context).cancelAllWorkByTag(type.name)

        // No return
        return@withContext
      }
}
