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

package com.pyamsoft.sleepforbreakfast.category.add

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface CategoryAddViewState : UiViewState {
  val name: StateFlow<String>
  val note: StateFlow<String>

  val color: StateFlow<Long>
  val showColorPicker: StateFlow<Boolean>

  val working: StateFlow<Boolean>
}

@Stable
class MutableCategoryAddViewState
@Inject
internal constructor(
    params: CategoryAddParams,
) : CategoryAddViewState {
  override val name = MutableStateFlow("")
  override val note = MutableStateFlow("")

  override val color = MutableStateFlow(params.categoryColor)
  override val showColorPicker = MutableStateFlow(false)

  override val working = MutableStateFlow(false)

  internal val existingCategory = MutableStateFlow<DbCategory?>(null)
}
