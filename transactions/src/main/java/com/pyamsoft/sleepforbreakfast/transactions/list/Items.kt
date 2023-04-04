package com.pyamsoft.sleepforbreakfast.transactions.list

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import java.time.Month

@Stable
internal sealed class TransactionOrHeader private constructor() {

  @Stable data class Transaction(val transaction: DbTransaction) : TransactionOrHeader()

  @Stable data class Header(val month: Month) : TransactionOrHeader()
}

@Composable
@CheckResult
internal fun rememberTransactionsWithHeaders(
    transactions: List<DbTransaction>
): List<TransactionOrHeader> {
  return remember(transactions) {
    if (transactions.isEmpty()) {
      return@remember emptyList()
    }

    val list = mutableListOf<TransactionOrHeader>()

    // Keep track of the last month
    var lastSeenMonth = transactions.first().date.month

    // Start by inserting the month header
    list.add(TransactionOrHeader.Header(lastSeenMonth))

    for (t in transactions) {
      val currentMonth = t.date.month

      // If the month changes, insert our month header
      if (currentMonth != lastSeenMonth) {
        lastSeenMonth = currentMonth
        list.add(TransactionOrHeader.Header(lastSeenMonth))
      }

      list.add(TransactionOrHeader.Transaction(t))
    }

    return@remember list
  }
}
