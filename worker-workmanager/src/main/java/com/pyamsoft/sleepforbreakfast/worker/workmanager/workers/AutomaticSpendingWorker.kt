package com.pyamsoft.sleepforbreakfast.worker.workmanager.workers

import android.content.Context
import androidx.work.WorkerParameters
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.sleepforbreakfast.worker.work.BgWorker
import com.pyamsoft.sleepforbreakfast.worker.work.automatictransaction.AutomaticSpendingWork
import com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerComponent
import javax.inject.Inject

class AutomaticSpendingWorker
internal constructor(
    context: Context,
    params: WorkerParameters,
) : AbstractJobWorker(context, params) {

  @Inject @JvmField internal var work: AutomaticSpendingWork? = null

  override fun onInject(component: WorkerComponent) {
    component.inject(this)
  }

  override fun worker(): BgWorker {
    return work.requireNotNull()
  }

  override fun onDestroy() {
    work = null
  }
}
