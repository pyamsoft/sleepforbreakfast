package com.pyamsoft.sleepforbreakfast.transactions.add

import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.money.list.ListAddInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class TransactionAddInteractorImpl
@Inject
constructor(
    private val transactionInsertDao: TransactionInsertDao,
) : TransactionAddInteractor, ListAddInteractorImpl<DbTransaction>() {

  override suspend fun performInsert(item: DbTransaction): DbInsert.InsertResult<DbTransaction> {
    return transactionInsertDao.insert(item)
  }
}
