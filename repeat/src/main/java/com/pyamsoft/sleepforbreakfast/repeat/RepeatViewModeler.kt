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

package com.pyamsoft.sleepforbreakfast.repeat

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatChangeEvent
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import com.pyamsoft.sleepforbreakfast.money.list.ListViewModeler
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddParams
import com.pyamsoft.sleepforbreakfast.repeat.delete.RepeatDeleteParams
import com.pyamsoft.sleepforbreakfast.ui.savedstate.JsonParser
import com.pyamsoft.sleepforbreakfast.ui.savedstate.fromJson
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

class RepeatViewModeler
@Inject
internal constructor(
    state: MutableRepeatViewState,
    interactor: RepeatInteractor,
    enforcer: ThreadEnforcer,
    private val categoryLoader: CategoryLoader,
    private val jsonParser: JsonParser,
) :
    RepeatViewState by state,
    ListViewModeler<DbRepeat, RepeatChangeEvent, MutableRepeatViewState>(
        enforcer = enforcer,
        state = state,
        interactor = interactor,
    ) {

  private fun handleAddParams(params: RepeatAddParams) {
    state.addParams.value = params
  }

  private fun handleDeleteParams(params: RepeatDeleteParams) {
    state.deleteParams.value = params
  }

  override fun CoroutineScope.onItemRealtimeEvent(event: RepeatChangeEvent) {
    when (event) {
      is RepeatChangeEvent.Delete -> {
        // Do not offer an undo since I do not know how to roll back the entire Cascade action
        handleItemDeleted(event.repeat, offerUndo = false)
      }
      is RepeatChangeEvent.Insert -> handleItemInserted(event.repeat)
      is RepeatChangeEvent.Update -> handleItemUpdated(event.repeat)
    }
  }

  override fun onConsumeRestoredState(registry: SaveableStateRegistry) {
    registry
        .consumeRestored(KEY_ADD_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<RepeatAddParams.Json>(it) }
        ?.fromJson()
        ?.let { handleAddParams(it) }

    registry
        .consumeRestored(KEY_DELETE_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<RepeatDeleteParams.Json>(it) }
        ?.fromJson()
        ?.let { handleDeleteParams(it) }
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

  override fun isMatchingSearch(item: DbRepeat, search: String): Boolean {
    return item.transactionName.contains(search, ignoreCase = true)
  }

  override fun isEqual(o1: DbRepeat, o2: DbRepeat): Boolean {
    return o1.id.raw == o2.id.raw
  }

  override fun List<DbRepeat>.sort(): List<DbRepeat> {
    return this.sortedByDescending { it.createdAt }
  }

  fun handleEditRepeat(repeat: DbRepeat) {
    handleAddParams(
        params =
            RepeatAddParams(
                repeatId = repeat.id,
            ),
    )
  }

  fun handleAddNewRepeat() {
    handleAddParams(
        params =
            RepeatAddParams(
                repeatId = DbRepeat.Id.EMPTY,
            ),
    )
  }

  fun handleCloseAddRepeat() {
    state.addParams.value = null
  }

  fun handleDeleteRepeat(repeat: DbRepeat) {
    handleDeleteParams(
        params = RepeatDeleteParams(repeatId = repeat.id),
    )
  }

  fun handleCloseDeleteRepeat() {
    state.deleteParams.value = null
  }

  companion object {
    private const val KEY_ADD_PARAMS = "key_add_params"
    private const val KEY_DELETE_PARAMS = "key_delete_params"
  }
}
