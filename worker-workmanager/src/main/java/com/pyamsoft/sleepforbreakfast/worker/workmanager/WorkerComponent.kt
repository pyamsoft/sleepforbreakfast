package com.pyamsoft.sleepforbreakfast.worker.workmanager

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.worker.workmanager.workers.AutomaticSpendingWorker
import com.pyamsoft.sleepforbreakfast.worker.workmanager.workers.RepeatCreateTransactionWorker
import dagger.Subcomponent

@Subcomponent
interface WorkerComponent {

  fun inject(worker: AutomaticSpendingWorker)

  fun inject(worker: RepeatCreateTransactionWorker)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult fun create(): WorkerComponent
  }
}
