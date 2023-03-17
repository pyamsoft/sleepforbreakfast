package com.pyamsoft.sleepforbreakfast.worker.workmanager

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.worker.workmanager.workers.AutomaticSpendingWorker
import dagger.Subcomponent

@Subcomponent
interface WorkerComponent {

  fun inject(worker: AutomaticSpendingWorker)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult fun create(): WorkerComponent
  }
}
