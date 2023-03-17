package com.pyamsoft.sleepforbreakfast.worker

import androidx.annotation.CheckResult

interface BgWorker {

  @CheckResult suspend fun work(): WorkResult

  sealed class WorkResult {
    object Success : WorkResult()
    object Cancelled : WorkResult()
    data class Failed(val throwable: Throwable) : WorkResult()
  }
}
