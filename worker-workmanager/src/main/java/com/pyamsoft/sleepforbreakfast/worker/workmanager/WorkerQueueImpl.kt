/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.worker.workmanager

import android.content.Context
import androidx.annotation.CheckResult
import androidx.work.Configuration
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.await
import com.pyamsoft.pydroid.core.cast
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.worker.WorkJobType
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import com.pyamsoft.sleepforbreakfast.worker.workmanager.workers.AutomaticSpendingWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

internal class WorkerQueueImpl
@Inject
internal constructor(
    private val context: Context,
) : WorkerQueue {

  /**
   * This method is called in rare cases to initialize WorkManager ourselves.
   */
  private fun initializeWorkManager() {
    try {
      /*
      Note: `WorkManager.getInstance(context)` should already initialize WorkManager when the
      application implements Configuration.Provider. However, as a further safeguard, let's detect
      for this when we attempt to initialize WorkManager.
       */
      val configuration =
        context.applicationContext.cast<Configuration.Provider>()?.workManagerConfiguration
          ?: Configuration.Builder().build()
      WorkManager.initialize(context, configuration)
    } catch (e: IllegalStateException) {
      /*
      This catch is meant for the exception -
      https://android.googlesource.com/platform/frameworks/support/+/60ae0eec2a32396c22ad92502cde952c80d514a0/work/workmanager/src/main/java/androidx/work/impl/WorkManagerImpl.java#177
      1. We lost the race with another call to  WorkManager.initialize outside of OneSignal.
      2. It is possible for some other unexpected error is thrown from WorkManager.
       */
      Timber.e(e) { "initializeWorkManager workaround failed" }
    }
  }

  /**
   * 07/18/2024
   *
   * Seeing a bunch of these after the M3 update
   * https://github.com/pyamsoft/sleepforbreakfast/commit/d4c070c201762da8c7504a21eac9baddd59d7ca2#diff-49a96e7eea8a94af862798a45174e6ac43eb4f8b4bd40759b5da63ba31ec3ef7
   *
   * Error queueing work: ONESHOT_AUTOMATIC_TRANSACTION java.lang.IllegalStateException: WorkManager
   * is not initialized properly. You have explicitly disabled WorkManagerInitializer in your
   * manifest, have not manually called WorkManager#initialize at this point, and your Application
   * does not implement Configuration.Provider. at
   * androidx.work.impl.WorkManagerImpl.getInstance(WorkManagerImpl.java:173) at
   * androidx.work.WorkManager.getInstance(WorkManager.java:184) at
   * com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerQueueImpl$enqueue$2.invokeSuspend(WorkerQueueImpl.kt:51)
   * at com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerQueueImpl$enqueue$2.invoke(Unknown
   * Source:8) at
   * com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerQueueImpl$enqueue$2.invoke(Unknown
   * Source:4) at
   * kotlinx.coroutines.intrinsics.UndispatchedKt.startUndispatchedOrReturn(Undispatched.kt:61) at
   * kotlinx.coroutines.BuildersKt__Builders_commonKt.withContext(Builders.common.kt:163) at
   * kotlinx.coroutines.BuildersKt.withContext(Unknown Source:1) at
   * com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerQueueImpl.enqueue(WorkerQueueImpl.kt:39)
   * at com.pyamsoft.sleepforbreakfast.work.ActivityWorkKt.enqueueActivityWork(ActivityWork.kt:25)
   * at
   * com.pyamsoft.sleepforbreakfast.main.MainActivity$beginWork$1.invokeSuspend(MainActivity.kt:55)
   * at kotlin.coroutines.jvm.internal.BaseContinuationImpl.resumeWith(ContinuationImpl.kt:33) at
   * kotlinx.coroutines.DispatchedTask.run(DispatchedTask.kt:104) at
   * kotlinx.coroutines.scheduling.CoroutineScheduler.runSafely(CoroutineScheduler.kt:584) at
   * kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.executeTask(CoroutineScheduler.kt:811)
   * at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.runWorker(CoroutineScheduler.kt:715)
   * at kotlinx.coroutines.scheduling.CoroutineScheduler$Worker.run(CoroutineScheduler.kt:702)
   *
   * Attempt this kind of fix https://github.com/OneSignal/OneSignal-Android-SDK/pull/2052/files
   *
   * https://github.com/OneSignal/OneSignal-Android-SDK/blob/main/OneSignalSDK/onesignal/notifications/src/main/java/com/onesignal/notifications/internal/common/OSWorkManagerHelper.kt
   */
  @CheckResult
  private fun ensureWorkManagerInitialized(): WorkManager {
    if (!WorkManager.isInitialized()) {
      synchronized(INIT_LOCK) {
        return try {
          WorkManager.getInstance(context)
        } catch (e: IllegalStateException) {
          /*
          This aims to catch the IllegalStateException "WorkManager is not initialized properly..." -
          https://androidx.tech/artifacts/work/work-runtime/2.8.1-source/androidx/work/impl/WorkManagerImpl.java.html
           */
          Timber.e(e) { "WorkManager.getInstance failed, attempting to initialize: " }
          initializeWorkManager()
          WorkManager.getInstance(context)
        }
      }
    }

    return WorkManager.getInstance(context)
  }

  override suspend fun enqueue(type: WorkJobType) =
      withContext(context = Dispatchers.Default) {
        val builder: WorkRequest.Builder<*, *> =
            when (type) {
              WorkJobType.ONESHOT_AUTOMATIC_TRANSACTION ->
                  OneTimeWorkRequestBuilder<AutomaticSpendingWorker>()
            }

        val work = builder.addTag(type.name).build()
        Timber.d { "Enqueue work: $type" }

        // Resolve the WorkManager instance
        try {
          ensureWorkManagerInitialized().enqueue(work).await()
        } catch (e: Throwable) {
          Timber.e(e) { "Error queueing work: $type" }
        }

        // No return
        return@withContext
      }

  override suspend fun cancel(type: WorkJobType) =
      withContext(context = Dispatchers.Default) {

        // Resolve the WorkManager instance
        Timber.d { "Cancel work by tag: $type" }
        try {
          ensureWorkManagerInitialized().cancelAllWorkByTag(type.name).await()
        } catch (e: Throwable) {
          Timber.e(e) { "Error cancelling work: $type" }
        }

        // No return
        return@withContext
      }

  companion object {
    private val INIT_LOCK = Any()
  }
}
