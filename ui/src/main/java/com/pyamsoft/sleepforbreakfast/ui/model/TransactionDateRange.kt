/*
 * Copyright 2025 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.ui.model

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@Stable
data class TransactionDateRange(
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
