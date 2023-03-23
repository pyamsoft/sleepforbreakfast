package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddInteractor
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddInteractorImpl
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteInteractor
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteInteractorImpl
import dagger.Binds
import dagger.Module

@Module
abstract class TransactionAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: TransactionInteractorImpl): TransactionInteractor

  @Binds
  @CheckResult
  internal abstract fun bindDeleteInteractor(
      impl: TransactionDeleteInteractorImpl
  ): TransactionDeleteInteractor

  @Binds
  @CheckResult
  internal abstract fun bindAddInteractor(
      impl: TransactionAddInteractorImpl
  ): TransactionAddInteractor
}
