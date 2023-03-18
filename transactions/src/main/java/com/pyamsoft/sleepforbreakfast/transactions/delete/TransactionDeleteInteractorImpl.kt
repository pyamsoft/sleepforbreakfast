package com.pyamsoft.sleepforbreakfast.transactions.delete

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDeleteDao
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
) : TransactionDeleteInteractor {

  override suspend fun deleteTransaction(transaction: DbTransaction): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          ResultWrapper.success(transactionDeleteDao.delete(transaction))
        } catch (e: Throwable) {
          Timber.e(e, "Error deleting transaction: $transaction")
          ResultWrapper.failure(e)
        }
      }
}
