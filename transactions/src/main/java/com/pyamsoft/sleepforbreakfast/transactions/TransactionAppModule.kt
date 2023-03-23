package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import dagger.Binds
import dagger.Module

@Module
abstract class TransactionAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: TransactionInteractorImpl): TransactionInteractor
}
