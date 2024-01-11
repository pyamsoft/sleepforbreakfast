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
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.util.preferenceBooleanFlow
import com.pyamsoft.sleepforbreakfast.db.DbPreferences
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

@Singleton
internal class PreferencesImpl
@Inject
internal constructor(
    private val context: Context,
    private val enforcer: ThreadEnforcer,
) : DbPreferences {

  private val preferences by lazy {
    enforcer.assertOffMainThread()
    PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
  }

  private val scope by lazy {
    CoroutineScope(
        context = Dispatchers.IO + SupervisorJob() + CoroutineName(this::class.java.name),
    )
  }

  override fun listenSystemCategoriesPreloaded(): Flow<Boolean> =
      preferenceBooleanFlow(KEY_DEFAULT_CATEGORIES, false) { preferences }
          .flowOn(context = Dispatchers.IO)

  override fun markSystemCategoriesPreloaded() {
    scope.launch { preferences.edit { putBoolean(KEY_DEFAULT_CATEGORIES, true) } }
  }

  companion object {

    private const val KEY_DEFAULT_CATEGORIES = "key_default_categories"
  }
}
