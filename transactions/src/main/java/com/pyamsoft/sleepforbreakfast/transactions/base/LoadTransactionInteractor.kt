package com.pyamsoft.sleepforbreakfast.transactions.base

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction

internal interface LoadTransactionInteractor {

  @CheckResult suspend fun load(transactionId: DbTransaction.Id): ResultWrapper<DbTransaction>
}
