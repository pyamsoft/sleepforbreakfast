package com.pyamsoft.sleepforbreakfast.spending

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.spending.automatic.AutomaticManager
import com.pyamsoft.sleepforbreakfast.spending.automatic.AutomaticManagerImpl
import com.pyamsoft.sleepforbreakfast.spending.db.SystemCategories
import com.pyamsoft.sleepforbreakfast.spending.db.SystemCategoriesImpl
import dagger.Binds
import dagger.Module

@Module
abstract class SpendingAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindAutomaticManager(impl: AutomaticManagerImpl): AutomaticManager

  @Binds
  @CheckResult
  internal abstract fun bindSpendingHandler(
      impl: SpendingTrackerHandlerImpl
  ): SpendingTrackerHandler

  @Binds
  @CheckResult
  internal abstract fun bindSystemCategories(impl: SystemCategoriesImpl): SystemCategories
}
