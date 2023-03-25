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

package com.pyamsoft.sleepforbreakfast.category

import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.category.add.CategoryAddParams
import com.pyamsoft.sleepforbreakfast.category.delete.CategoryDeleteParams
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.list.ListViewState
import com.pyamsoft.sleepforbreakfast.money.list.MutableListViewState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface CategoryViewState : ListViewState<DbCategory> {
  val addParams: StateFlow<CategoryAddParams?>
  val deleteParams: StateFlow<CategoryDeleteParams?>
}

@Stable
class MutableCategoryViewState @Inject internal constructor() :
    CategoryViewState, MutableListViewState<DbCategory>() {
  override val addParams = MutableStateFlow<CategoryAddParams?>(null)
  override val deleteParams = MutableStateFlow<CategoryDeleteParams?>(null)
}
