package com.pyamsoft.sleepforbreakfast.worker

import androidx.annotation.CheckResult

interface WorkJob {

  @get:CheckResult val type: Type

  enum class Type {
    AUTOMATIC_SPENDING_CONVERTER
  }
}
