package com.pyamsoft.sleepforbreakfast.repeat

import androidx.annotation.CheckResult
import dagger.Binds
import dagger.Module

@Module
abstract class RepeatAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: RepeatInteractorImpl): RepeatInteractor
}
