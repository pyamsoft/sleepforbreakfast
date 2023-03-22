package com.pyamsoft.sleepforbreakfast.work

import com.pyamsoft.sleepforbreakfast.worker.WorkJobType
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue

internal suspend fun WorkerQueue.enqueueActivityWork() {
  // Queue up the oneshot Repeat job for immediate processing
  this.cancel(WorkJobType.ONESHOT_CREATE_TRANSACTIONS)
  this.enqueue(WorkJobType.ONESHOT_CREATE_TRANSACTIONS)

  // Queue up the oneshot Automatic job for immediate processing
  this.cancel(WorkJobType.ONESHOT_AUTOMATIC_TRANSACTION)
  this.enqueue(WorkJobType.ONESHOT_AUTOMATIC_TRANSACTION)
}
