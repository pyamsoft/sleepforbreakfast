package com.pyamsoft.sleepforbreakfast.repeat

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddInteractor
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddInteractorImpl
import com.pyamsoft.sleepforbreakfast.repeat.delete.RepeatDeleteInteractor
import com.pyamsoft.sleepforbreakfast.repeat.delete.RepeatDeleteInteractorImpl
import dagger.Binds
import dagger.Module

@Module
abstract class RepeatAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindDeleteInteractor(
      impl: RepeatDeleteInteractorImpl
  ): RepeatDeleteInteractor

  @Binds
  @CheckResult
  internal abstract fun bindAddInteractor(impl: RepeatAddInteractorImpl): RepeatAddInteractor

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: RepeatInteractorImpl): RepeatInteractor
}
