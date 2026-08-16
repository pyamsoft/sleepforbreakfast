/*
 * Copyright 2026 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.util.AppDispatchers
import com.pyamsoft.sleepforbreakfast.automatic.AutomaticAppModule
import com.pyamsoft.sleepforbreakfast.category.CategoryAppModule
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.DbModule
import com.pyamsoft.sleepforbreakfast.db.DbPreferences
import com.pyamsoft.sleepforbreakfast.db.room.RoomModule
import com.pyamsoft.sleepforbreakfast.main.MainActivity
import com.pyamsoft.sleepforbreakfast.main.MainComponent
import com.pyamsoft.sleepforbreakfast.money.MoneyAppModule
import com.pyamsoft.sleepforbreakfast.preference.PreferencesImpl
import com.pyamsoft.sleepforbreakfast.service.SpendingTrackerService
import com.pyamsoft.sleepforbreakfast.spending.SpendingAppModule
import com.pyamsoft.sleepforbreakfast.spending.guaranteed.SpendingGuaranteeAppModule
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
import kotlinx.coroutines.CoroutineScope

private val Context.dataStore by
    preferencesDataStore(
        name = "sleepforbreakfast_preferences",
        corruptionHandler =
            ReplaceFileCorruptionHandler { err ->
              Timber.e(err) { "File corruption detected, start with empty Preferences" }
              return@ReplaceFileCorruptionHandler emptyPreferences()
            },
        produceMigrations = { migrationContext ->
          listOf(
              // NOTE(Peter): Since our shared preferences was the DEFAULT process one, loading up
              //              a migration without specifying all keys will also migrate
              //              PYDROID SPECIFIC PREFERENCES which is what we do NOT want to do.
              //              We instead maintain ONLY a list of the known app preference keys
              SharedPreferencesMigration(
                  keysToMigrate =
                      setOf(
                          PreferenceKeys.KEY_DEFAULT_CATEGORIES.name,
                      ),
                  produceSharedPreferences = {
                    PreferenceManager.getDefaultSharedPreferences(
                        migrationContext.applicationContext
                    )
                  },
              ),
          )
        },
    )

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
            AutomaticAppModule::class,

            // DB
            DbModule::class,
            RoomModule::class,

            // Work
            WorkManagerAppModule::class,

            // Spending
            SpendingAppModule::class,
            SpendingGuaranteeAppModule::class,
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
        @Named("app_scope") @BindsInstance scope: CoroutineScope,
        @Named("debug") @BindsInstance debug: Boolean,
        @BindsInstance application: Application,
        @BindsInstance theming: Theming,
        @BindsInstance enforcer: ThreadEnforcer,
        @BindsInstance dispatchers: AppDispatchers,
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
      @Singleton
      internal fun provideDataStore(context: Context): DataStore<Preferences> {
        return context.applicationContext.dataStore
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
