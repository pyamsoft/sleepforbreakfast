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
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class ListViewModeler<T : Any, CE : Any, S : MutableListViewState<T>>
protected constructor(
    final override val state: S,
    private val interactor: ListInteractor<*, T, CE>,
) : AbstractViewModeler<S>(state) {

  private val allItems = MutableStateFlow(emptyList<T>())

  private val submitRunner =
      highlander<ResultWrapper<DbInsert.InsertResult<T>>, T> { interactor.submit(it) }

  private fun listenForItems(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      interactor.listenForItemChanges { onItemRealtimeEvent(it) }
    }
  }

  private data class ItemPayload<T : Any>(
      val items: List<T>,
      val search: String,
  )

  private fun generateItemsBasedOnAllItems(scope: CoroutineScope) {
    // Create a source that generates data based on the latest from all sources
    val combined =
        combineTransform(
            allItems,
            state.search,
        ) { all, search ->
          emit(
              ItemPayload(
                  items = all,
                  search = search,
              ),
          )
        }

    scope.launch(context = Dispatchers.Default) {
      combined.collect { (all, search) ->
        if (search.isBlank()) {
          state.items.value = all.sort()
        } else {
          state.items.value = all.filter { isMatchingSearch(it, search) }.sort()
        }
      }
    }
  }

  final override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        registry.registerProvider(KEY_SEARCH) { state.search.value }.also { add(it) }
        registry.registerProvider(KEY_IS_SEARCH_OPEN) { state.isSearchOpen.value }.also { add(it) }

        onRegisterSaveState(registry)
      }

  final override fun consumeRestoredState(registry: SaveableStateRegistry) {
    registry.consumeRestored(KEY_SEARCH)?.let { it as String }?.also { state.search.value = it }

    registry
        .consumeRestored(KEY_IS_SEARCH_OPEN)
        ?.let { it as Boolean }
        ?.also { state.isSearchOpen.value = it }

    onConsumeRestoredState(registry)
  }

  protected fun handleItemDeleted(
      item: T,
      offerUndo: Boolean,
  ) {
    allItems.update { list -> list.filterNot { isEqual(it, item) } }

    if (offerUndo) {
      Timber.d("Offer undo on item delete: $item")
      state.recentlyDeleted.value = item
    }
  }

  protected fun handleItemUpdated(item: T) {
    allItems.update { list -> list.map { if (isEqual(it, item)) item else it } }
  }

  protected fun handleItemInserted(item: T) {
    allItems.update { list -> (list + item) }
  }

  fun bind(scope: CoroutineScope) {
    handleRefresh(
        scope = scope,
        force = false,
    )

    listenForItems(scope = scope)

    generateItemsBasedOnAllItems(scope = scope)
  }

  fun handleRefresh(
      scope: CoroutineScope,
      force: Boolean,
  ) {
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
          .onSuccess { Timber.d("Loaded items list: $it") }
          .onSuccess { items ->
            allItems.value = items
            state.itemError.value = null
          }
          .onFailure { Timber.e(it, "Error loading items") }
          .onFailure { err ->
            allItems.value = emptyList()
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

  fun handleToggleSearch() {
    state.isSearchOpen.update { !it }
  }

  fun handleSearchUpdated(search: String) {
    state.search.value = search
  }

  protected abstract fun CoroutineScope.onItemRealtimeEvent(event: CE)

  @CheckResult protected abstract fun isEqual(o1: T, o2: T): Boolean

  @CheckResult
  protected abstract fun isMatchingSearch(
      item: T,
      search: String,
  ): Boolean

  @CheckResult protected abstract fun List<T>.sort(): List<T>

  protected abstract fun MutableList<SaveableStateRegistry.Entry>.onRegisterSaveState(
      registry: SaveableStateRegistry
  )

  protected abstract fun onConsumeRestoredState(registry: SaveableStateRegistry)

  companion object {
    private const val KEY_IS_SEARCH_OPEN = "key_is_search_open"
    private const val KEY_SEARCH = "key_search"
  }
}
