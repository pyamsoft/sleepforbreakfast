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

package com.pyamsoft.sleepforbreakfast.money

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.SpendDirection

@CheckResult
fun Collection<DbTransaction>.calculateTotalTransactionAmount(): Long {
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
fun Long.calculateTotalTransactionDirection(): SpendDirection {
  val self = this
  return if (self == 0L) {
    SpendDirection.NONE
  } else if (self < 0) {
    SpendDirection.SPEND
  } else {
    SpendDirection.EARN
  }
}

@CheckResult
fun Collection<DbTransaction>.calculateTotalTransactionRange(): String {
  val self = this
  val first = self.firstOrNull()
  val last = self.lastOrNull()

  // No transactions, no note
  if (first == null) {
    return ""
  }

  // If we only have one transaction
  if (last == null || last.date == first.date) {
    val dateString = DATE_FORMATTER.format(first.date)
    return "On $dateString"
  }

  val firstDateString = DATE_FORMATTER.format(first.date)
  val lastDateString = DATE_FORMATTER.format(last.date)
  return "From $lastDateString to $firstDateString"
}
