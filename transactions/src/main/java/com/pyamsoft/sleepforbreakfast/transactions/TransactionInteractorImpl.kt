package com.pyamsoft.sleepforbreakfast.transactions

import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionChangeEvent
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDeleteDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionRealtime
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TransactionInteractorImpl
@Inject
constructor(
    private val transactionRealtime: TransactionRealtime,
    private val transactionInsertDao: TransactionInsertDao,
    private val transactionDeleteDao: TransactionDeleteDao,
    private val transactionQueryDao: TransactionQueryDao,
    private val transactionQueryCache: TransactionQueryDao.Cache,
) :
    TransactionInteractor,
    ListInteractorImpl<DbTransaction.Id, DbTransaction, TransactionChangeEvent>() {

  override suspend fun performQueryAll(): List<DbTransaction> {
    return transactionQueryDao.query()
  }

  override suspend fun performQueryOne(id: DbTransaction.Id): Maybe<out DbTransaction> {
    return transactionQueryDao.queryById(id)
  }

  override suspend fun performClearCache() {
    transactionQueryCache.invalidate()
  }

  override suspend fun performClearCache(id: DbTransaction.Id) {
    transactionQueryCache.invalidateById(id)
  }

  override suspend fun performListenRealtime(onEvent: (TransactionChangeEvent) -> Unit) {
    transactionRealtime.listenForChanges(onEvent)
  }

  override suspend fun performInsert(item: DbTransaction): DbInsert.InsertResult<DbTransaction> {
    return transactionInsertDao.insert(item)
  }

  override suspend fun performDelete(item: DbTransaction): Boolean {
    return transactionDeleteDao.delete(item)
  }
}
