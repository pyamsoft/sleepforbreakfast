package com.pyamsoft.sleepforbreakfast.sources

import androidx.annotation.CheckResult
import dagger.Binds
import dagger.Module

@Module
abstract class SourcesAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: SourcesInteractorImpl): SourcesInteractor
}
