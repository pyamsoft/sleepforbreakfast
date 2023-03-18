package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionChangeEvent

internal interface TransactionInteractor {

  @CheckResult suspend fun loadAll(force: Boolean): ResultWrapper<List<DbTransaction>>

  @CheckResult suspend fun listenToTransactions(onEvent: (TransactionChangeEvent) -> Unit)

  @CheckResult
  suspend fun restoreTransaction(
      transaction: DbTransaction
  ): ResultWrapper<DbInsert.InsertResult<DbTransaction>>
}
