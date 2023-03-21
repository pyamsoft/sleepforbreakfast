package com.pyamsoft.sleepforbreakfast.worker.workmanager

import android.content.Context
import androidx.annotation.CheckResult
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pyamsoft.sleepforbreakfast.worker.WorkJobType
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import com.pyamsoft.sleepforbreakfast.worker.workmanager.workers.AutomaticSpendingWorker
import com.pyamsoft.sleepforbreakfast.worker.workmanager.workers.RepeatCreateTransactionWorker
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

  @CheckResult
  private suspend fun workManager(): WorkManager =
      withContext(context = Dispatchers.IO) { WorkManager.getInstance(context) }

  override suspend fun enqueue(job: WorkJobType) {
    val builder =
        when (job) {
          WorkJobType.AUTOMATIC_SPENDING_CONVERTER ->
              OneTimeWorkRequestBuilder<AutomaticSpendingWorker>()
          WorkJobType.REPEAT_CREATE_TRANSACTIONS ->
              OneTimeWorkRequestBuilder<RepeatCreateTransactionWorker>()
        }

    val work = builder.addTag(job.name).build()
    Timber.d("Enqueue work: $job $work")
    workManager().enqueue(work)
  }

  override suspend fun cancel(type: WorkJobType) {
    workManager().cancelAllWorkByTag(type.name)
  }

  override suspend fun cancelAll() {
    workManager().cancelAllWork()
  }
}
