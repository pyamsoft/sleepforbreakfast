package com.pyamsoft.sleepforbreakfast.transactions.delete

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.transactions.base.SingleTransactionInteractor

internal interface TransactionDeleteInteractor : SingleTransactionInteractor {

  @CheckResult suspend fun deleteTransaction(transaction: DbTransaction): ResultWrapper<Boolean>
}
