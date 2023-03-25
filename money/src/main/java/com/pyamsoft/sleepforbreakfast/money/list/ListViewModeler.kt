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

package com.pyamsoft.sleepforbreakfast.money.list

import androidx.annotation.CheckResult
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class ListViewModeler<T : Any, CE : Any, S : MutableListViewState<T>>
protected constructor(
    final override val state: S,
    private val interactor: ListInteractor<*, T, CE>,
) : AbstractViewModeler<S>(state) {

  private val submitRunner =
      highlander<ResultWrapper<DbInsert.InsertResult<T>>, T> { interactor.submit(it) }

  private fun listenForItems(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      interactor.listenForItemChanges { onItemRealtimeEvent(it) }
    }
  }

  protected abstract fun CoroutineScope.onItemRealtimeEvent(event: CE)

  @CheckResult protected abstract fun isEqual(o1: T, o2: T): Boolean

  @CheckResult protected abstract fun List<T>.sort(): List<T>

  protected fun handleItemDeleted(
      item: T,
      offerUndo: Boolean,
  ) {
    val s = state
    s.items.update { list -> list.filterNot { isEqual(it, item) }.sort() }

    if (offerUndo) {
      Timber.d("Offer undo on item delete: $item")
      s.recentlyDeleted.value = item
    }
  }

  protected fun handleItemUpdated(item: T) {
    state.items.update { list -> list.map { if (isEqual(it, item)) item else it }.sort() }
  }

  protected fun handleItemInserted(item: T) {
    state.items.update { list -> (list + item).sort() }
  }

  fun bind(scope: CoroutineScope) {
    handleRefresh(
        scope = scope,
        force = false,
    )

    listenForItems(scope = scope)
  }

  fun handleRefresh(scope: CoroutineScope, force: Boolean) {
    if (state.loadingState.value == LoadingState.LOADING) {
      Timber.w("Already loading items list")
      return
    }

    scope.launch(context = Dispatchers.Main) {
      if (state.loadingState.value == LoadingState.LOADING) {
        Timber.w("Already loading items list")
        return@launch
      }

      state.loadingState.value = LoadingState.LOADING
      interactor
          .loadAll(force = force)
          .map { list -> list.sort() }
          .onSuccess { Timber.d("Loaded items list: $it") }
          .onSuccess { sources ->
            state.items.value = sources
            state.itemError.value = null
          }
          .onFailure { Timber.e(it, "Error loading items") }
          .onFailure { err ->
            state.items.value = emptyList()
            state.itemError.value = err
          }
          .onFinally { state.loadingState.value = LoadingState.DONE }
    }
  }

  fun handleDeleteFinalized() {
    val deleted = state.recentlyDeleted.getAndUpdate { null }
    if (deleted != null) {
      // Once finally deleted, don't offer undo
      handleItemDeleted(deleted, offerUndo = false)
    }
  }

  fun handleRestoreDeleted(scope: CoroutineScope) {
    val deleted = state.recentlyDeleted.getAndUpdate { null }
    if (deleted != null) {
      scope.launch(context = Dispatchers.Main) {
        submitRunner
            .call(deleted)
            .onFailure { Timber.e(it, "Error when restoring $deleted") }
            .onSuccess { result ->
              when (result) {
                is DbInsert.InsertResult.Insert -> Timber.d("Restored: ${result.data}")
                is DbInsert.InsertResult.Update -> Timber.d("Updated: ${result.data} from $deleted")
                is DbInsert.InsertResult.Fail -> {
                  Timber.e(result.error, "Failed to restore: $deleted")
                  // Caught by the onFailure below
                  throw result.error
                }
              }
            }
            .onFailure {
              Timber.e(it, "Failed to restore")
              // TODO handle restore error
            }
      }
    }
  }
}
