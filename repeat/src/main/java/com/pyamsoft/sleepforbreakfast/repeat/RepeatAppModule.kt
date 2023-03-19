package com.pyamsoft.sleepforbreakfast.repeat

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.money.helper.LoadExistingHandler
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddInteractor
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddInteractorImpl
import com.pyamsoft.sleepforbreakfast.repeat.base.LoadRepeatInteractor
import com.pyamsoft.sleepforbreakfast.repeat.base.LoadRepeatInteractorImpl
import com.pyamsoft.sleepforbreakfast.repeat.base.RepeatLoadHandler
import dagger.Binds
import dagger.Module

@Module
abstract class RepeatAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindLoadRepeatHandler(
      impl: RepeatLoadHandler
  ): LoadExistingHandler<DbRepeat.Id, DbRepeat>

  @Binds
  @CheckResult
  internal abstract fun bindTransactionAddInteractor(
      impl: RepeatAddInteractorImpl
  ): RepeatAddInteractor

  @Binds
  @CheckResult
  internal abstract fun bindLoadInteractor(impl: LoadRepeatInteractorImpl): LoadRepeatInteractor
}
