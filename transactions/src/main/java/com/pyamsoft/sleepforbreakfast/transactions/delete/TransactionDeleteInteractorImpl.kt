package com.pyamsoft.sleepforbreakfast.transactions.delete

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDeleteDao
import com.pyamsoft.sleepforbreakfast.money.list.ListDeleteInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TransactionDeleteInteractorImpl
@Inject
constructor(
    private val transactionDeleteDao: TransactionDeleteDao,
) : TransactionDeleteInteractor, ListDeleteInteractorImpl<DbTransaction>() {

  override suspend fun performDelete(item: DbTransaction): Boolean {
    return transactionDeleteDao.delete(item)
  }
}
