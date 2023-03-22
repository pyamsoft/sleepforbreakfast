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

package com.pyamsoft.sleepforbreakfast.sources

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.sources.add.SourcesAddParams
import com.pyamsoft.sleepforbreakfast.sources.delete.SourcesDeleteParams
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface SourcesViewState : UiViewState {
  val loadingState: StateFlow<LoadingState>
  val sources: StateFlow<List<DbSource>>
  val sourceError: StateFlow<Throwable?>

  val addParams: StateFlow<SourcesAddParams?>
  val deleteParams: StateFlow<SourcesDeleteParams?>

  val recentlyDeleteSource: StateFlow<DbSource?>

  @Stable
  @Immutable
  enum class LoadingState {
    NONE,
    LOADING,
    DONE
  }
}

@Stable
class MutableSourcesViewState @Inject internal constructor() : SourcesViewState {
  override val loadingState = MutableStateFlow(SourcesViewState.LoadingState.NONE)
  override val sources = MutableStateFlow(emptyList<DbSource>())
  override val sourceError = MutableStateFlow<Throwable?>(null)

  override val addParams = MutableStateFlow<SourcesAddParams?>(null)
  override val deleteParams = MutableStateFlow<SourcesDeleteParams?>(null)

  override val recentlyDeleteSource = MutableStateFlow<DbSource?>(null)
}
