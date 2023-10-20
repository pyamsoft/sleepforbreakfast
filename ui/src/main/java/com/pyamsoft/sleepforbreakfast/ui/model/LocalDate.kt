package com.pyamsoft.sleepforbreakfast.ui.model

import androidx.annotation.CheckResult
import java.time.LocalDate
import java.time.LocalDateTime

@CheckResult
fun LocalDate.atEndOfDay(): LocalDateTime {
  return this.atStartOfDay().plusDays(1).minusMinutes(1)
}
