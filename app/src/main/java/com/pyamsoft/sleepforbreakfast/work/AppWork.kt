package com.pyamsoft.sleepforbreakfast.work

import com.pyamsoft.sleepforbreakfast.worker.WorkJobType
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue

internal suspend fun WorkerQueue.enqueueAppWork() {
  // Queue up the periodic Repeat job for processing once-a-day
  this.cancel(WorkJobType.REPEAT_CREATE_TRANSACTIONS)
  this.enqueue(WorkJobType.REPEAT_CREATE_TRANSACTIONS)

  // And run any activity work (in case the Activity is not opened later)
  this.enqueueActivityWork()
}
