package com.pyamsoft.sleepforbreakfast.worker.workmanager

import android.content.Context
import androidx.annotation.CheckResult
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.pyamsoft.sleepforbreakfast.worker.WorkJob
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import com.pyamsoft.sleepforbreakfast.worker.workmanager.workers.AutomaticSpendingWorker
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

  override suspend fun enqueue(job: WorkJob) {
    val builder =
        when (job.type) {
          WorkJob.Type.AUTOMATIC_SPENDING_CONVERTER ->
              OneTimeWorkRequestBuilder<AutomaticSpendingWorker>()
        }

    val work = builder.addTag(job.type.name).build()
    Timber.d("Enqueue work: $job $work")
    workManager().enqueue(work)
  }

  override suspend fun cancel(type: WorkJob.Type) {
    workManager().cancelAllWorkByTag(type.name)
  }

  override suspend fun cancelAll() {
    workManager().cancelAllWork()
  }
}
