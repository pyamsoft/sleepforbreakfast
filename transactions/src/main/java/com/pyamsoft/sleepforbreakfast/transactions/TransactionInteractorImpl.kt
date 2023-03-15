package com.pyamsoft.sleepforbreakfast.transactions

import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionChangeEvent
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDeleteDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionRealtime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class TransactionInteractorImpl
@Inject
constructor(
    private val transactionRealtime: TransactionRealtime,
    private val transactionInsertDao: TransactionInsertDao,
    private val transactionDeleteDao: TransactionDeleteDao,
    private val transactionQueryDao: TransactionQueryDao,
    private val transactionQueryCache: TransactionQueryDao.Cache,
) : TransactionInteractor {

  override suspend fun loadAll(force: Boolean): ResultWrapper<List<DbTransaction>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          transactionQueryCache.invalidate()
        }

        return@withContext try {
          ResultWrapper.success(transactionQueryDao.query())
        } catch (e: Throwable) {
          Timber.e(e, "Error loading transactions")
          ResultWrapper.failure(e)
        }
      }

  override suspend fun listenToTransactions(onEvent: (TransactionChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext transactionRealtime.listenForChanges(onEvent)
      }

  override suspend fun restoreTransaction(
      transaction: DbTransaction
  ): ResultWrapper<DbInsert.InsertResult<DbTransaction>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          ResultWrapper.success(transactionInsertDao.insert(transaction))
        } catch (e: Throwable) {
          Timber.e(e, "Error restoring transaction: $transaction")
          ResultWrapper.failure(e)
        }
      }
}
