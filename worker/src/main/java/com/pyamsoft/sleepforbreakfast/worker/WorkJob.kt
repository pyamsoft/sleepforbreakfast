package com.pyamsoft.sleepforbreakfast.worker

import androidx.annotation.CheckResult

interface WorkJob {

  @get:CheckResult val type: WorkJobType
}
