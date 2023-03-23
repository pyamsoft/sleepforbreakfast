package com.pyamsoft.sleepforbreakfast.sources

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.money.helper.LoadExistingHandler
import com.pyamsoft.sleepforbreakfast.sources.add.SourceAddInteractor
import com.pyamsoft.sleepforbreakfast.sources.add.SourceAddInteractorImpl
import com.pyamsoft.sleepforbreakfast.sources.base.SourceLoadHandler
import dagger.Binds
import dagger.Module

@Module
abstract class SourcesAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindLoadRepeatHandler(
      impl: SourceLoadHandler
  ): LoadExistingHandler<DbSource.Id, DbSource>

  @Binds
  @CheckResult
  internal abstract fun bindAddInteractor(impl: SourceAddInteractorImpl): SourceAddInteractor

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: SourcesInteractorImpl): SourcesInteractor
}
