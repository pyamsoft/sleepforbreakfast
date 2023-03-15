package com.pyamsoft.sleepforbreakfast.transactions.delete

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDeleteDao
import com.pyamsoft.sleepforbreakfast.transactions.InternalApi
import com.pyamsoft.sleepforbreakfast.transactions.base.SingleTransactionInteractor
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class TransactionDeleteInteractorImpl
@Inject
constructor(
    private val transactionDeleteDao: TransactionDeleteDao,
    @InternalApi private val single: SingleTransactionInteractor,
) : TransactionDeleteInteractor {

  override suspend fun load(transactionId: DbTransaction.Id): ResultWrapper<DbTransaction> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext single.load(transactionId)
      }

  override suspend fun deleteTransaction(transaction: DbTransaction): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          ResultWrapper.success(transactionDeleteDao.delete(transaction, offerUndo = true))
        } catch (e: Throwable) {
          Timber.e(e, "Error deleting transaction: $transaction")
          ResultWrapper.failure(e)
        }
      }
}
