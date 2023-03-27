/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.SpendDirection
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val DATE_RANGE_FORMATTER =
    object : ThreadLocal<DateTimeFormatter>() {

      override fun initialValue(): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
      }
    }

@CheckResult
internal fun List<DbTransaction>.calculateTotalTransactionAmount(): Long {
  val self = this
  var total: Long = 0
  for (transaction in self) {
    total +=
        when (transaction.type) {
          DbTransaction.Type.SPEND -> -transaction.amountInCents
          DbTransaction.Type.EARN -> +transaction.amountInCents
        }
  }

  return total
}

@CheckResult
internal fun Long.calculateTotalTransactionDirection(): SpendDirection {
  val self = this
  return if (self == 0L) {
    SpendDirection.NONE
  } else if (self < 0) {
    SpendDirection.LOSS
  } else {
    SpendDirection.GAIN
  }
}

@CheckResult
internal fun List<DbTransaction>.calculateTotalTransactionRange(): String {
  val self = this
  val first = self.firstOrNull()
  val last = self.lastOrNull()

  // No transactions, no note
  if (first == null) {
    return ""
  }

  val formatter = DATE_RANGE_FORMATTER.get().requireNotNull()

  // If we only have one transaction
  if (last == null || last.date == first.date) {
    val dateString = formatter.format(first.date)
    return "From $dateString"
  }

  val firstDateString = formatter.format(first.date)
  val lastDateString = formatter.format(last.date)
  return "From $lastDateString to $firstDateString"
}
