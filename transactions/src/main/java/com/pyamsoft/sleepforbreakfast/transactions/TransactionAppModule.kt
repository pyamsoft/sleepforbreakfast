package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddInteractor
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddInteractorImpl
import com.pyamsoft.sleepforbreakfast.transactions.base.DeleteRestoreHandler
import com.pyamsoft.sleepforbreakfast.transactions.base.DeleteRestoreHandlerImpl
import com.pyamsoft.sleepforbreakfast.transactions.base.LoadTransactionHandler
import com.pyamsoft.sleepforbreakfast.transactions.base.LoadTransactionHandlerImpl
import com.pyamsoft.sleepforbreakfast.transactions.base.LoadTransactionInteractor
import com.pyamsoft.sleepforbreakfast.transactions.base.LoadTransactionInteractorImpl
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteInteractor
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteInteractorImpl
import dagger.Binds
import dagger.Module

@Module
abstract class TransactionAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindLoadTransactionHandler(
      impl: LoadTransactionHandlerImpl
  ): LoadTransactionHandler

  @Binds
  @CheckResult
  internal abstract fun bindDeleteRestoreHandler(
      impl: DeleteRestoreHandlerImpl
  ): DeleteRestoreHandler

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
