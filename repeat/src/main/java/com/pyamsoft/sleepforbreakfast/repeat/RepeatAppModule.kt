package com.pyamsoft.sleepforbreakfast.repeat

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddInteractor
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddInteractorImpl
import com.pyamsoft.sleepforbreakfast.repeat.base.LoadRepeatHandler
import com.pyamsoft.sleepforbreakfast.repeat.base.LoadRepeatHandlerImpl
import com.pyamsoft.sleepforbreakfast.repeat.base.LoadRepeatInteractor
import com.pyamsoft.sleepforbreakfast.repeat.base.LoadRepeatInteractorImpl
import dagger.Binds
import dagger.Module

@Module
abstract class RepeatAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindLoadRepeatHandler(impl: LoadRepeatHandlerImpl): LoadRepeatHandler

  @Binds
  @CheckResult
  internal abstract fun bindTransactionAddInteractor(
      impl: RepeatAddInteractorImpl
  ): RepeatAddInteractor

  @Binds
  @CheckResult
  internal abstract fun bindLoadInteractor(impl: LoadRepeatInteractorImpl): LoadRepeatInteractor
}
