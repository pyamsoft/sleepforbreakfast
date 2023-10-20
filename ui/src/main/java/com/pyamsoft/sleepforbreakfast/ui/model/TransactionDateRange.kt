package com.pyamsoft.sleepforbreakfast.ui.model

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Stable
data class TransactionDateRange
internal constructor(
    val from: LocalDate,
    val to: LocalDate,
) {

  @CheckResult
  fun toBundleable(): Pair<Long, Long> {
    val fromSeconds = from.atStartOfDay().toEpochSecond(ZoneOffset.UTC)
    val toSeconds = to.atEndOfDay().toEpochSecond(ZoneOffset.UTC)
    return fromSeconds to toSeconds
  }

  companion object {

    @CheckResult
    private fun Long.secondsToLocalDate(): LocalDate {
      return Instant.ofEpochSecond(this).atOffset(ZoneOffset.UTC).toLocalDate()
    }

    @JvmStatic
    @CheckResult
    fun fromBundleable(
        from: Long,
        to: Long,
    ): TransactionDateRange? {
      return TransactionDateRange(
          from = from.secondsToLocalDate(),
          to = to.secondsToLocalDate(),
      )
    }
  }
}

@CheckResult
fun LocalDate.toDateRange(to: LocalDate): TransactionDateRange {
  return TransactionDateRange(
      from = this,
      to = to,
  )
}
