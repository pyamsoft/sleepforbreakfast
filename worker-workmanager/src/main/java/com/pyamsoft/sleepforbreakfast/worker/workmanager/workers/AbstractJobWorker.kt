package com.pyamsoft.sleepforbreakfast.worker.workmanager.workers

import android.content.Context
import androidx.annotation.CheckResult
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pyamsoft.sleepforbreakfast.worker.work.BgWorker
import com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerComponent
import com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerObjectGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class AbstractJobWorker
protected constructor(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

  private fun inject() {
    onInject(WorkerObjectGraph.retrieve(applicationContext))
  }

  private fun destroy() {
    onDestroy()
  }

  final override suspend fun doWork(): Result =
      withContext(context = Dispatchers.Default) {
        try {
          inject()

          return@withContext process(worker())
        } catch (e: Throwable) {
          Timber.e(e, "Error running work")
          return@withContext Result.failure()
        } finally {
          destroy()
        }
      }

  protected abstract fun onInject(component: WorkerComponent)

  protected abstract fun onDestroy()

  protected abstract fun worker(): BgWorker

  companion object {

    @CheckResult
    private suspend fun process(worker: BgWorker): Result {
      return when (val result = worker.work()) {
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
  }
}
