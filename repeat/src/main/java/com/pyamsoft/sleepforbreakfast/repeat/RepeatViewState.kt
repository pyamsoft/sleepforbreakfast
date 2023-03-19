/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.sleepforbreakfast.repeat

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddParams
import com.pyamsoft.sleepforbreakfast.repeat.delete.RepeatDeleteParams
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface RepeatViewState : UiViewState {
  val loadingState: StateFlow<LoadingState>
  val repeats: StateFlow<List<DbRepeat>>
  val repeatError: StateFlow<Throwable?>

  val addParams: StateFlow<RepeatAddParams?>
  val deleteParams: StateFlow<RepeatDeleteParams?>

  val recentlyDeleteRepeat: StateFlow<DbRepeat?>

  @Stable
  @Immutable
  enum class LoadingState {
    NONE,
    LOADING,
    DONE
  }
}

@Stable
class MutableRepeatViewState @Inject internal constructor() : RepeatViewState {
  override val addParams = MutableStateFlow<RepeatAddParams?>(null)
  override val deleteParams = MutableStateFlow<RepeatDeleteParams?>(null)

  override val recentlyDeleteRepeat = MutableStateFlow<DbRepeat?>(null)

  override val loadingState = MutableStateFlow(RepeatViewState.LoadingState.NONE)
  override val repeats = MutableStateFlow(emptyList<DbRepeat>())
  override val repeatError = MutableStateFlow<Throwable?>(null)
}
