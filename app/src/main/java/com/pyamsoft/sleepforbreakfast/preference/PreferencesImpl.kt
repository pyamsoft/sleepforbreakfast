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

package com.pyamsoft.sleepforbreakfast.preference

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.pyamsoft.pydroid.core.LintIgnoreTooGenericExceptionCaught
import com.pyamsoft.pydroid.util.AppDispatchers
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.sleepforbreakfast.PreferenceKeys
import com.pyamsoft.sleepforbreakfast.core.AppCoroutineScope
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.DbPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

@Singleton
internal class PreferencesImpl
@Inject
internal constructor(
    private val appScope: AppCoroutineScope,
    private val dispatchers: AppDispatchers,
    dataStore: DataStore<Preferences>,
) : DbPreferences {

  private val preferences = dataStore

  private inline fun <T : Any> setPreference(
      key: Preferences.Key<T>,
      fallbackValue: T,
      crossinline value: suspend (Preferences) -> T,
  ) {
    appScope.launch(context = dispatchers.io) {
      try {
        preferences.edit { it[key] = value(it) }
      } catch (@LintIgnoreTooGenericExceptionCaught e: Throwable) {
        e.ifNotCancellation { preferences.edit { it[key] = fallbackValue } }
      }
    }
  }

  private fun <T : Any> getPreference(
      key: Preferences.Key<T>,
      value: T,
  ): Flow<T> =
      preferences.data
          .map { it[key] ?: value }
          .catch { err ->
            Timber.e(err) { "Error reading from dataStore: ${key.name}" }
            preferences.edit { it[key] = value }
            emit(value)
          }

  override fun listenSystemCategoriesPreloaded(): Flow<Boolean> =
      getPreference(
              key = PreferenceKeys.KEY_DEFAULT_CATEGORIES,
              value = DEFAULT_DEFAULT_CATEGORIES,
          )
          .flowOn(context = dispatchers.io)

  override fun markSystemCategoriesPreloaded() =
      setPreference(
          key = PreferenceKeys.KEY_DEFAULT_CATEGORIES,
          fallbackValue = DEFAULT_DEFAULT_CATEGORIES,
          value = { true },
      )

  companion object {

    private const val DEFAULT_DEFAULT_CATEGORIES = false
  }
}
