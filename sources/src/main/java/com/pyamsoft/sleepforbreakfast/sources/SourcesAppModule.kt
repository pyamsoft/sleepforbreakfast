package com.pyamsoft.sleepforbreakfast.sources

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.sources.add.SourceAddInteractor
import com.pyamsoft.sleepforbreakfast.sources.add.SourceAddInteractorImpl
import com.pyamsoft.sleepforbreakfast.sources.delete.SourcesDeleteInteractor
import com.pyamsoft.sleepforbreakfast.sources.delete.SourcesDeleteInteractorImpl
import dagger.Binds
import dagger.Module

@Module
abstract class SourcesAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindDeleteInteractor(
      impl: SourcesDeleteInteractorImpl
  ): SourcesDeleteInteractor

  @Binds
  @CheckResult
  internal abstract fun bindAddInteractor(impl: SourceAddInteractorImpl): SourceAddInteractor

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: SourcesInteractorImpl): SourcesInteractor
}
