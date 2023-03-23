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

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.source.SourceChangeEvent
import com.pyamsoft.sleepforbreakfast.money.list.ListViewModeler
import com.pyamsoft.sleepforbreakfast.sources.add.SourceAddInteractor
import com.pyamsoft.sleepforbreakfast.sources.add.SourcesAddParams
import com.pyamsoft.sleepforbreakfast.sources.delete.SourcesDeleteParams
import com.pyamsoft.sleepforbreakfast.ui.savedstate.JsonParser
import com.pyamsoft.sleepforbreakfast.ui.savedstate.fromJson
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

class SourcesViewModeler
@Inject
internal constructor(
    state: MutableSourcesViewState,
    interactor: SourcesInteractor,
    addInteractor: SourceAddInteractor,
    private val jsonParser: JsonParser,
) :
    ListViewModeler<DbSource, SourceChangeEvent, MutableSourcesViewState>(
        state = state,
        interactor = interactor,
        addInteractor = addInteractor,
    ) {

  private fun handleAddParams(params: SourcesAddParams) {
    state.addParams.value = params
  }

  private fun handleDeleteParams(params: SourcesDeleteParams) {
    state.deleteParams.value = params
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
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

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    registry
        .consumeRestored(KEY_ADD_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<SourcesAddParams.Json>(it) }
        ?.fromJson()
        ?.let { handleAddParams(it) }

    registry
        .consumeRestored(KEY_DELETE_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<SourcesDeleteParams.Json>(it) }
        ?.fromJson()
        ?.let { handleDeleteParams(it) }
  }

  override fun CoroutineScope.onItemRealtimeEvent(event: SourceChangeEvent) {
    when (event) {
      is SourceChangeEvent.Delete ->
          // When actually deleted from the DB, offer undo ability
          handleItemDeleted(event.source, offerUndo = true)
      is SourceChangeEvent.Insert -> handleItemInserted(event.source)
      is SourceChangeEvent.Update -> handleItemUpdated(event.source)
    }
  }

  override fun isEqual(o1: DbSource, o2: DbSource): Boolean {
    return o1.id.raw == o2.id.raw
  }

  override fun List<DbSource>.sort(): List<DbSource> {
    return this.sortedByDescending { it.createdAt }
  }

  fun handleEditSources(source: DbSource) {
    handleAddParams(
        params =
            SourcesAddParams(
                sourcesId = source.id,
            ),
    )
  }

  fun handleAddNewSources() {
    handleAddParams(
        params =
            SourcesAddParams(
                sourcesId = DbSource.Id.EMPTY,
            ),
    )
  }

  fun handleCloseAddSource() {
    state.addParams.value = null
  }

  fun handleDeleteSource(source: DbSource) {
    handleDeleteParams(
        params =
            SourcesDeleteParams(
                sourcesId = source.id,
            ),
    )
  }

  fun handleCloseDeleteSource() {
    state.deleteParams.value = null
  }

  companion object {
    private const val KEY_ADD_PARAMS = "key_add_params"
    private const val KEY_DELETE_PARAMS = "key_delete_params"
  }
}
