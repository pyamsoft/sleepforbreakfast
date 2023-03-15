package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val DATE_RANGE_FORMATTER =
    object : ThreadLocal<DateTimeFormatter>() {

      override fun initialValue(): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
      }
    }

internal enum class SpendDirection {
  NONE,
  LOSS,
  GAIN
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
internal fun DbTransaction.Type.asDirection(): SpendDirection =
    when (this) {
      DbTransaction.Type.SPEND -> SpendDirection.LOSS
      DbTransaction.Type.EARN -> SpendDirection.GAIN
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
