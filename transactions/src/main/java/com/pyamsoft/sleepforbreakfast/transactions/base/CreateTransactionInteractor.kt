package com.pyamsoft.sleepforbreakfast.transactions.base

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction

interface CreateTransactionInteractor {

  @CheckResult
  suspend fun submit(
      transaction: DbTransaction
  ): ResultWrapper<DbInsert.InsertResult<DbTransaction>>
}
