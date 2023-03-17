package com.pyamsoft.sleepforbreakfast.worker.workmanager.workers

import android.content.Context
import androidx.annotation.CheckResult
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.sleepforbreakfast.worker.BgWorker
import com.pyamsoft.sleepforbreakfast.worker.work.AutomaticSpendingWork
import com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerObjectGraph
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class AutomaticSpendingWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

  @Inject @JvmField internal var work: AutomaticSpendingWork? = null

  private fun inject() {
    if (work == null) {
      WorkerObjectGraph.retrieve(applicationContext).inject(this)
    }
  }

  private fun onDestroy() {
    work = null
  }

  @CheckResult
  private suspend fun process(): Result {
    return when (val result = work.requireNotNull().work()) {
      is BgWorker.WorkResult.Cancelled -> {
        Timber.w("Work was cancelled, report success to avoid retry policy")
        Result.success()
      }
      is BgWorker.WorkResult.Failed -> {
        Timber.e(result.throwable, "Work failed to complete")
        Result.failure()
      }
      is BgWorker.WorkResult.Success -> {
        Timber.d("Work succeeded")
        Result.success()
      }
    }
  }

  override suspend fun doWork(): Result =
      withContext(context = Dispatchers.Default) {
        try {
          inject()

          return@withContext process()
        } catch (e: Throwable) {
          Timber.e(e, "Error running work")
          return@withContext Result.failure()
        } finally {
          onDestroy()
        }
      }
}
