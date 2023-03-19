package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.helper.DeleteRestoreHandler
import com.pyamsoft.sleepforbreakfast.money.helper.LoadExistingHandler
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddInteractor
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddInteractorImpl
import com.pyamsoft.sleepforbreakfast.transactions.base.LoadTransactionInteractor
import com.pyamsoft.sleepforbreakfast.transactions.base.LoadTransactionInteractorImpl
import com.pyamsoft.sleepforbreakfast.transactions.base.TransactionDeleteRestoreHandler
import com.pyamsoft.sleepforbreakfast.transactions.base.TransactionLoadHandler
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteInteractor
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteInteractorImpl
import dagger.Binds
import dagger.Module

@Module
abstract class TransactionAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindLoadTransactionHandler(
      impl: TransactionLoadHandler
  ): LoadExistingHandler<DbTransaction.Id, DbTransaction>

  @Binds
  @CheckResult
  internal abstract fun bindDeleteRestoreHandler(
      impl: TransactionDeleteRestoreHandler
  ): DeleteRestoreHandler<DbTransaction>

  @Binds
  @CheckResult
  internal abstract fun bindLoadInteractor(
      impl: LoadTransactionInteractorImpl
  ): LoadTransactionInteractor

  @Binds
  @CheckResult
  internal abstract fun bindTransactionInteractor(
      impl: TransactionInteractorImpl
  ): TransactionInteractor

  @Binds
  @CheckResult
  internal abstract fun bindTransactionDeleteInteractor(
      impl: TransactionDeleteInteractorImpl
  ): TransactionDeleteInteractor

  @Binds
  @CheckResult
  internal abstract fun bindTransactionAddInteractor(
      impl: TransactionAddInteractorImpl
  ): TransactionAddInteractor
}
