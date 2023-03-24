package com.pyamsoft.sleepforbreakfast.category

import androidx.annotation.CheckResult
import dagger.Binds
import dagger.Module

@Module
abstract class CategoryAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: CategoryInteractorImpl): CategoryInteractor
}
