/*
 * Copyright 2024 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.preference

import android.content.Context
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import com.pyamsoft.sleepforbreakfast.db.DbPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

@Singleton
internal class PreferencesImpl
@Inject
internal constructor(
    private val context: Context,
) : DbPreferences {

  private val Context.dataStore by
      preferencesDataStore(
          name = "sleepforbreakfast_preferences",
          produceMigrations = {
            listOf(
                // NOTE(Peter): Since our shared preferences was the DEFAULT process one, loading up
                //              a migration without specifying all keys will also migrate
                //              PYDROID SPECIFIC PREFERENCES which is what we do NOT want to do.
                //              We instead maintain ONLY a list of the known app preference keys
                SharedPreferencesMigration(
                    keysToMigrate =
                        setOf(
                            KEY_DEFAULT_CATEGORIES.name,
                        ),
                    produceSharedPreferences = {
                      PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                    },
                ),
            )
          },
      )

  private val preferences by lazy { context.applicationContext.dataStore }

  private val scope by lazy {
    CoroutineScope(
        context = Dispatchers.IO + SupervisorJob() + CoroutineName(this::class.java.name),
    )
  }

  private inline fun setPreference(crossinline block: suspend (MutablePreferences) -> Unit) {
    scope.launch(context = Dispatchers.IO) { preferences.edit { block(it) } }
  }

  override fun listenSystemCategoriesPreloaded(): Flow<Boolean> =
      preferences.data
          .map { it[KEY_DEFAULT_CATEGORIES] ?: DEFAULT_DEFAULT_CATEGORIES }
          .flowOn(context = Dispatchers.IO)

  override fun markSystemCategoriesPreloaded() {
    setPreference { it[KEY_DEFAULT_CATEGORIES] = true }
  }

  companion object {

    private val KEY_DEFAULT_CATEGORIES = booleanPreferencesKey("key_default_categories")
    private const val DEFAULT_DEFAULT_CATEGORIES = false
  }
}
