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

package com.pyamsoft.sleepforbreakfast.home

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface HomeViewState : UiViewState {
  val loading: StateFlow<LoadingState>
  val isNotificationListenerEnabled: StateFlow<Boolean>
  val categories: StateFlow<List<DbCategory>>
  val transactionsByCategory: StateFlow<Map<DbCategory.Id, Set<DbTransaction>>>
  val transactionsByDateRange: StateFlow<Map<DayRange, Set<DbTransaction>>>

  enum class DayRange {
    DAY,
    WEEK,
    MONTH
  }
}

@Stable
class MutableHomeViewState @Inject internal constructor() : HomeViewState {
  override val loading = MutableStateFlow(LoadingState.NONE)
  override val isNotificationListenerEnabled = MutableStateFlow(false)
  override val categories = MutableStateFlow(emptyList<DbCategory>())
  override val transactionsByCategory =
      MutableStateFlow(emptyMap<DbCategory.Id, Set<DbTransaction>>())
  override val transactionsByDateRange =
      MutableStateFlow(emptyMap<HomeViewState.DayRange, Set<DbTransaction>>())
}
