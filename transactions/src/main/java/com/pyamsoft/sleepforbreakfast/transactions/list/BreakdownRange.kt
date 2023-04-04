package com.pyamsoft.sleepforbreakfast.transactions.list

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.squareup.moshi.JsonClass
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Stable
data class BreakdownRange
internal constructor(
    val start: LocalDate,
    val end: LocalDate,
) {

  @CheckResult
  fun toJson(): Json {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    return Json(
        start = formatter.format(start),
        end = formatter.format(end),
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json(
      val start: String,
      val end: String,
  ) {

    @CheckResult
    fun fromJson(): BreakdownRange {
      val formatter = DateTimeFormatter.ISO_LOCAL_DATE
      return BreakdownRange(
          start = LocalDate.parse(start, formatter),
          end = LocalDate.parse(end, formatter),
      )
    }
  }

  companion object {
    @CheckResult
    fun now(clock: Clock): BreakdownRange {
      val now = LocalDate.now(clock)
      return BreakdownRange(
          start = now.minusMonths(1),
          end = now,
      )
    }
  }
}
