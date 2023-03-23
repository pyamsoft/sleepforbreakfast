package com.pyamsoft.sleepforbreakfast.transactions.base

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.helper.LoadExistingHandlerImpl
import com.pyamsoft.sleepforbreakfast.transactions.TransactionInteractor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionLoadHandler
@Inject
internal constructor(
    private val interactor: TransactionInteractor,
) : LoadExistingHandlerImpl<DbTransaction.Id, DbTransaction>() {

  override fun isIdEmpty(id: DbTransaction.Id): Boolean {
    return id.isEmpty
  }

  override suspend fun loadData(id: DbTransaction.Id): ResultWrapper<DbTransaction> {
    return interactor.loadOne(id)
  }
}
