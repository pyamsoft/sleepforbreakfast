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

package com.pyamsoft.sleepforbreakfast.automatic

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.core.cast
import com.pyamsoft.sleepforbreakfast.automatic.add.AutomaticAddParams
import com.pyamsoft.sleepforbreakfast.automatic.delete.AutomaticDeleteParams
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotification
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationChangeEvent
import com.pyamsoft.sleepforbreakfast.money.list.ListViewModeler
import com.pyamsoft.sleepforbreakfast.ui.savedstate.JsonParser
import com.pyamsoft.sleepforbreakfast.ui.savedstate.fromJson
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

class AutomaticViewModeler
@Inject
internal constructor(
    state: MutableAutomaticViewState,
    interactor: AutomaticInteractor,
    enforcer: ThreadEnforcer,
    private val jsonParser: JsonParser,
) :
    AutomaticViewState by state,
    ListViewModeler<DbNotificationWithRegexes, NotificationChangeEvent, MutableAutomaticViewState>(
        enforcer = enforcer,
        state = state,
        interactor = interactor,
    ) {

  private fun handleAddParams(params: AutomaticAddParams) {
    state.addParams.value = params
  }

  private fun handleDeleteParams(params: AutomaticDeleteParams) {
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
        ?.cast<String>()
        ?.let { jsonParser.fromJson<AutomaticAddParams.Json>(it) }
        ?.fromJson()
        ?.let { handleAddParams(it) }

    registry
        .consumeRestored(KEY_DELETE_PARAMS)
        ?.cast<String>()
        ?.let { jsonParser.fromJson<AutomaticDeleteParams.Json>(it) }
        ?.fromJson()
        ?.let { handleDeleteParams(it) }
  }

  override fun CoroutineScope.onItemRealtimeEvent(event: NotificationChangeEvent) {
    when (event) {
      is NotificationChangeEvent.Delete -> {
        // Do not offer an undo since I do not know how to roll back the entire Cascade action
        handleItemDeleted(event.notification, offerUndo = false)
      }
      is NotificationChangeEvent.Insert -> handleItemInserted(event.notification)
      is NotificationChangeEvent.Update -> handleItemUpdated(event.notification)
    }
  }

  override fun isMatchingSearch(item: DbNotificationWithRegexes, search: String): Boolean {
    return item.notification.name.contains(search, ignoreCase = true)
  }

  override fun isEqual(o1: DbNotificationWithRegexes, o2: DbNotificationWithRegexes): Boolean {
    return o1.notification.id.raw == o2.notification.id.raw
  }

  override fun List<DbNotificationWithRegexes>.sort(): List<DbNotificationWithRegexes> {
    return this.sortedBy { it.notification.name }
  }

  fun handleEditAutomatic(notification: DbNotificationWithRegexes) {
    handleAddParams(
        params =
            AutomaticAddParams(
                notificationId = notification.notification.id,
            ),
    )
  }

  fun handleAddNewAutomatic() {
    handleAddParams(
        params =
            AutomaticAddParams(
                notificationId = DbNotification.Id.EMPTY,
            ),
    )
  }

  fun handleCloseAddAutomatic() {
    state.addParams.value = null
  }

  fun handleDeleteAutomatic(notification: DbNotificationWithRegexes) {
    handleDeleteParams(
        params =
            AutomaticDeleteParams(
                notificationId = notification.notification.id,
            ),
    )
  }

  fun handleCloseDeleteAutomatic() {
    state.deleteParams.value = null
  }

  companion object {
    private const val KEY_ADD_PARAMS = "key_add_params"
    private const val KEY_DELETE_PARAMS = "key_delete_params"
  }
}
