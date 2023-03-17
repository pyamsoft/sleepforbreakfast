package com.pyamsoft.sleepforbreakfast.transactions.add

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.transactions.InternalApi
import com.pyamsoft.sleepforbreakfast.transactions.base.CreateTransactionInteractor
import com.pyamsoft.sleepforbreakfast.transactions.base.SingleTransactionInteractor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class TransactionAddInteractorImpl
@Inject
constructor(
    @InternalApi private val create: CreateTransactionInteractor,
    @InternalApi private val single: SingleTransactionInteractor,
) : TransactionAddInteractor {
  override suspend fun load(transactionId: DbTransaction.Id): ResultWrapper<DbTransaction> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext single.load(transactionId)
      }

  override suspend fun submit(
      transaction: DbTransaction
  ): ResultWrapper<DbInsert.InsertResult<DbTransaction>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext create.submit(transaction)
      }
}
