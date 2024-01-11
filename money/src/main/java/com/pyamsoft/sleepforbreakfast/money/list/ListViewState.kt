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

package com.pyamsoft.sleepforbreakfast.money.list

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface ListViewState<T : Any> : UiViewState {
  val loadingState: StateFlow<LoadingState>
  val items: StateFlow<List<T>>
  val itemError: StateFlow<Throwable?>

  val recentlyDeleted: StateFlow<T?>

  val isSearchOpen: StateFlow<Boolean>
  val search: StateFlow<String>
}

abstract class MutableListViewState<T : Any> protected constructor() : ListViewState<T> {
  final override val loadingState = MutableStateFlow(LoadingState.NONE)
  final override val items = MutableStateFlow(emptyList<T>())
  final override val itemError = MutableStateFlow<Throwable?>(null)

  final override val recentlyDeleted = MutableStateFlow<T?>(null)

  final override val isSearchOpen = MutableStateFlow(false)
  final override val search = MutableStateFlow("")
}
