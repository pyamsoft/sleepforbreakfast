package com.pyamsoft.sleepforbreakfast.transactions.delete

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction

internal interface TransactionDeleteInteractor {

  @CheckResult suspend fun deleteTransaction(transaction: DbTransaction): ResultWrapper<Boolean>
}
