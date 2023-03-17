package com.pyamsoft.sleepforbreakfast.spending

import androidx.annotation.CheckResult
import dagger.Binds
import dagger.Module

@Module
abstract class SpendingAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindSpendingHandler(
      impl: SpendingTrackerHandlerImpl
  ): SpendingTrackerHandler
}
