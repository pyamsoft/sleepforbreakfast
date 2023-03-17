package com.pyamsoft.sleepforbreakfast.worker

interface WorkerQueue {

  suspend fun enqueue(job: WorkJob)

  suspend fun cancel(type: WorkJob.Type)

  suspend fun cancelAll()
}
