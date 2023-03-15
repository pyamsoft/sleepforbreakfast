package com.pyamsoft.sleepforbreakfast.transactions.base

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class CreateTransactionInteractorImpl
@Inject
constructor(
    private val transactionInsertDao: TransactionInsertDao,
) : CreateTransactionInteractor {

  override suspend fun submit(
      transaction: DbTransaction
  ): ResultWrapper<DbInsert.InsertResult<DbTransaction>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          ResultWrapper.success(transactionInsertDao.insert(transaction))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error submitting transaction: $transaction")
            ResultWrapper.failure(e)
          }
        }
      }
}
