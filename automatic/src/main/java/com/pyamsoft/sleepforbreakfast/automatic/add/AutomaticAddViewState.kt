/*
 * Copyright 2025 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.automatic.add

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface AutomaticAddViewState : UiViewState {
  val name: StateFlow<String>
  val type: StateFlow<DbTransaction.Type>
  val enabled: StateFlow<Boolean>

  val actOnPackageNames: StateFlow<List<String>>
  val workingRegexes: StateFlow<List<BuildMatchRegex>>

  val working: StateFlow<Boolean>

  data class BuildMatchRegex(
      val id: String,
      val text: String,
  )
}

@Stable
class MutableAutomaticAddViewState @Inject internal constructor() : AutomaticAddViewState {
  override val name = MutableStateFlow("")
  override val type = MutableStateFlow(DbTransaction.Type.SPEND)
  override val enabled = MutableStateFlow(true)

  override val actOnPackageNames = MutableStateFlow(emptyList<String>())
  override val workingRegexes = MutableStateFlow(emptyList<AutomaticAddViewState.BuildMatchRegex>())

  override val working = MutableStateFlow(false)
  internal val existingAutomatic = MutableStateFlow<DbNotificationWithRegexes?>(null)
}
