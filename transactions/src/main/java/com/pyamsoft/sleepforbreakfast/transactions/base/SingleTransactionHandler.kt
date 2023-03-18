package com.pyamsoft.sleepforbreakfast.transactions.base

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import kotlinx.coroutines.CoroutineScope

interface SingleTransactionHandler {

  fun loadExistingTransaction(
      scope: CoroutineScope,
      transactionId: DbTransaction.Id,
      onLoaded: (DbTransaction) -> Unit,
  )
}
