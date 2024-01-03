/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.sleepforbreakfast.category.CategoryAppModule
import com.pyamsoft.sleepforbreakfast.db.DbModule
import com.pyamsoft.sleepforbreakfast.db.DbPreferences
import com.pyamsoft.sleepforbreakfast.db.room.RoomModule
import com.pyamsoft.sleepforbreakfast.main.MainActivity
import com.pyamsoft.sleepforbreakfast.main.MainComponent
import com.pyamsoft.sleepforbreakfast.money.MoneyAppModule
import com.pyamsoft.sleepforbreakfast.preference.PreferencesImpl
import com.pyamsoft.sleepforbreakfast.service.SpendingTrackerService
import com.pyamsoft.sleepforbreakfast.spending.AutomaticHandlersAppModule
import com.pyamsoft.sleepforbreakfast.spending.SpendingAppModule
import com.pyamsoft.sleepforbreakfast.transactions.TransactionAppModule
import com.pyamsoft.sleepforbreakfast.ui.UiAppModule
import com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkManagerAppModule
import com.pyamsoft.sleepforbreakfast.worker.workmanager.WorkerComponent
import dagger.Binds
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import dagger.Provides
import java.time.Clock
import javax.inject.Named
import javax.inject.Singleton

@Singleton
@Component(
    modules =
        [
            BreakfastComponent.Provider::class,

            // UI
            TransactionAppModule::class,
            UiAppModule::class,
            CategoryAppModule::class,
            MoneyAppModule::class,

            // DB
            DbModule::class,
            RoomModule::class,

            // Work
            WorkManagerAppModule::class,

            // Spending
            SpendingAppModule::class,
            AutomaticHandlersAppModule::class,
        ],
)
internal interface BreakfastComponent {

  fun inject(application: SleepForBreakfast)

  fun inject(service: SpendingTrackerService)

  @CheckResult fun plusMainComponent(): MainComponent.Factory

  @CheckResult fun plusWorkerComponent(): WorkerComponent.Factory

  @Component.Factory
  interface Factory {

    @CheckResult
    fun create(
        @Named("debug") @BindsInstance debug: Boolean,
        @BindsInstance application: Application,
        @BindsInstance theming: Theming,
        @BindsInstance enforcer: ThreadEnforcer,
    ): BreakfastComponent
  }

  @Module
  abstract class Provider {

    @Binds internal abstract fun provideDbPreferences(impl: PreferencesImpl): DbPreferences

    @Module
    companion object {

      @Provides
      @JvmStatic
      internal fun provideActivityClass(): Class<out Activity> {
        return MainActivity::class.java
      }

      @Provides
      @JvmStatic
      internal fun provideContext(application: Application): Context {
        return application
      }

      @Provides
      @JvmStatic
      @Named("app_name")
      internal fun provideAppNameRes(): Int {
        return R.string.app_name
      }

      @Provides
      @JvmStatic
      @CheckResult
      internal fun provideClock(): Clock {
        return Clock.systemDefaultZone()
      }
    }
  }
}
