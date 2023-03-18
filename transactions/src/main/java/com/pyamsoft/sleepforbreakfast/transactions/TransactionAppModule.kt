package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddInteractor
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddInteractorImpl
import com.pyamsoft.sleepforbreakfast.transactions.base.CreateTransactionInteractor
import com.pyamsoft.sleepforbreakfast.transactions.base.CreateTransactionInteractorImpl
import com.pyamsoft.sleepforbreakfast.transactions.base.DeleteRestoreHandler
import com.pyamsoft.sleepforbreakfast.transactions.base.DeleteRestoreHandlerImpl
import com.pyamsoft.sleepforbreakfast.transactions.base.SingleTransactionHandler
import com.pyamsoft.sleepforbreakfast.transactions.base.SingleTransactionHandlerImpl
import com.pyamsoft.sleepforbreakfast.transactions.base.SingleTransactionInteractor
import com.pyamsoft.sleepforbreakfast.transactions.base.SingleTransactionInteractorImpl
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteInteractor
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteInteractorImpl
import dagger.Binds
import dagger.Module
import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) internal annotation class InternalApi

@Module
abstract class TransactionAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindSingleTransactionHandler(
      impl: SingleTransactionHandlerImpl
  ): SingleTransactionHandler

  @Binds
  @CheckResult
  internal abstract fun bindDeleteRestoreHandler(
      impl: DeleteRestoreHandlerImpl
  ): DeleteRestoreHandler

  @Binds
  @CheckResult
  internal abstract fun bindSingleInteractor(
      impl: SingleTransactionInteractorImpl
  ): SingleTransactionInteractor

  @Binds
  @CheckResult
  internal abstract fun bindTransactionAddInteractor(
      impl: TransactionAddInteractorImpl
  ): TransactionAddInteractor

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
  @InternalApi
  internal abstract fun bindSingleTransactionInteractor(
      impl: SingleTransactionInteractorImpl
  ): SingleTransactionInteractor

  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindCreateTransactionInteractor(
      impl: CreateTransactionInteractorImpl
  ): CreateTransactionInteractor
}
