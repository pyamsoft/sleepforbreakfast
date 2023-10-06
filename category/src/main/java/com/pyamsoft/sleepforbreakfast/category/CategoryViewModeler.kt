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

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.sleepforbreakfast.category.add.CategoryAddParams
import com.pyamsoft.sleepforbreakfast.category.delete.CategoryDeleteParams
import com.pyamsoft.sleepforbreakfast.db.category.CategoryChangeEvent
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.list.ListViewModeler
import com.pyamsoft.sleepforbreakfast.ui.savedstate.JsonParser
import com.pyamsoft.sleepforbreakfast.ui.savedstate.fromJson
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

class CategoryViewModeler
@Inject
internal constructor(
    state: MutableCategoryViewState,
    interactor: CategoryInteractor,
    enforcer: ThreadEnforcer,
    private val jsonParser: JsonParser,
) :
    CategoryViewState by state,
    ListViewModeler<DbCategory, CategoryChangeEvent, MutableCategoryViewState>(
        enforcer = enforcer,
        state = state,
        interactor = interactor,
    ) {

  private fun handleAddParams(params: CategoryAddParams) {
    state.addParams.value = params
  }

  private fun handleDeleteParams(params: CategoryDeleteParams) {
    state.deleteParams.value = params
  }

  override fun MutableList<SaveableStateRegistry.Entry>.onRegisterSaveState(
      registry: SaveableStateRegistry
  ) {
    registry
        .registerProvider(KEY_ADD_PARAMS) {
          state.addParams.value?.let { jsonParser.toJson(it.toJson()) }
        }
        .also { add(it) }

    registry
        .registerProvider(KEY_DELETE_PARAMS) {
          state.deleteParams.value?.let { jsonParser.toJson(it.toJson()) }
        }
        .also { add(it) }
  }

  override fun onConsumeRestoredState(registry: SaveableStateRegistry) {
    registry
        .consumeRestored(KEY_ADD_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<CategoryAddParams.Json>(it) }
        ?.fromJson()
        ?.let { handleAddParams(it) }

    registry
        .consumeRestored(KEY_DELETE_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<CategoryDeleteParams.Json>(it) }
        ?.fromJson()
        ?.let { handleDeleteParams(it) }
  }

  override fun CoroutineScope.onItemRealtimeEvent(event: CategoryChangeEvent) {
    when (event) {
      is CategoryChangeEvent.Delete -> {
        // Do not offer an undo since I do not know how to roll back the entire Cascade action
        handleItemDeleted(event.category, offerUndo = false)
      }
      is CategoryChangeEvent.Insert -> handleItemInserted(event.category)
      is CategoryChangeEvent.Update -> handleItemUpdated(event.category)
    }
  }

  override fun isMatchingSearch(item: DbCategory, search: String): Boolean {
    return item.name.contains(search, ignoreCase = true)
  }

  override fun isEqual(o1: DbCategory, o2: DbCategory): Boolean {
    return o1.id.raw == o2.id.raw
  }

  override fun List<DbCategory>.sort(): List<DbCategory> {
    return this.sortedBy { it.name }
  }

  fun handleEditCategory(category: DbCategory) {
    handleAddParams(
        params =
            CategoryAddParams(
                categoryId = category.id,
                categoryColor = category.color,
            ),
    )
  }

  fun handleAddNewCategory() {
    handleAddParams(
        params =
            CategoryAddParams(
                categoryId = DbCategory.Id.EMPTY,
                categoryColor = 0L,
            ),
    )
  }

  fun handleCloseAddCategory() {
    state.addParams.value = null
  }

  fun handleDeleteCategory(category: DbCategory) {
    handleDeleteParams(
        params =
            CategoryDeleteParams(
                categoryId = category.id,
            ),
    )
  }

  fun handleCloseDeleteCategory() {
    state.deleteParams.value = null
  }

  companion object {
    private const val KEY_ADD_PARAMS = "key_add_params"
    private const val KEY_DELETE_PARAMS = "key_delete_params"
  }
}
