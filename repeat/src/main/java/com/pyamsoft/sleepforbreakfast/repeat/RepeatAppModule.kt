package com.pyamsoft.sleepforbreakfast.repeat

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddInteractor
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddInteractorImpl
import dagger.Binds
import dagger.Module

@Module
abstract class RepeatAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindAddInteractor(impl: RepeatAddInteractorImpl): RepeatAddInteractor

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: RepeatInteractorImpl): RepeatInteractor
}
