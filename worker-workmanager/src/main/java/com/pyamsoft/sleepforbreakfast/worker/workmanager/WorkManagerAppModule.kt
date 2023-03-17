package com.pyamsoft.sleepforbreakfast.worker.workmanager

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import dagger.Binds
import dagger.Module

@Module
abstract class WorkManagerAppModule {

  @Binds @CheckResult internal abstract fun bindWorkerQueue(impl: WorkerQueueImpl): WorkerQueue
}
