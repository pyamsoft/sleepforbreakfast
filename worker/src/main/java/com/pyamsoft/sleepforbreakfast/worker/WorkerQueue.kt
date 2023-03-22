package com.pyamsoft.sleepforbreakfast.worker

interface WorkerQueue {

  suspend fun enqueue(type: WorkJobType)

  suspend fun cancel(type: WorkJobType)
}
