package com.pyamsoft.sleepforbreakfast.sources

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.sources.add.SourceAddInteractor
import com.pyamsoft.sleepforbreakfast.sources.add.SourceAddInteractorImpl
import dagger.Binds
import dagger.Module

@Module
abstract class SourcesAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindAddInteractor(impl: SourceAddInteractorImpl): SourceAddInteractor

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: SourcesInteractorImpl): SourcesInteractor
}
