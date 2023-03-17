package com.pyamsoft.sleepforbreakfast.transactions.base

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class SingleTransactionInteractorImpl
@Inject
constructor(
    private val transactionQueryDao: TransactionQueryDao,
) : SingleTransactionInteractor {

  override suspend fun load(transactionId: DbTransaction.Id): ResultWrapper<DbTransaction> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          when (val transaction = transactionQueryDao.queryById(transactionId)) {
            is Maybe.Data -> {
              ResultWrapper.success(transaction.data)
            }
            is Maybe.None -> {
              val err = RuntimeException("Could not find transaction with ID $transactionId")
              Timber.w(err.message)
              ResultWrapper.failure(err)
            }
          }
        } catch (e: Throwable) {
          Timber.e(e, "Error loading transaction $transactionId")
          ResultWrapper.failure(e)
        }
      }
}
