/*
 * Copyright 2025 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.money.delete

import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractor
import com.pyamsoft.sleepforbreakfast.money.one.OneViewModeler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class DeleteViewModeler<I : Any, T : Any, S : MutableDeleteViewState<T>>
protected constructor(
    state: S,
    initialId: I,
    private val interactor: ListInteractor<I, T, *>,
) :
    OneViewModeler<I, T, S>(
        state = state,
        initialId = initialId,
        interactor = interactor,
    ) {

  final override fun onBind(scope: CoroutineScope) {}

  final override fun CoroutineScope.onDataLoaded(result: T) {
    state.item.value = result
  }

  fun handleDelete(
      scope: CoroutineScope,
      onDeleted: (T) -> Unit,
  ) {
    if (state.working.value) {
      Timber.w { "Already deleting" }
      return
    }

    val item = state.item.value
    if (item == null) {
      Timber.w { "No item, cannot delete" }
      return
    }

    scope.launch(context = Dispatchers.Default) {
      if (state.working.value) {
        Timber.w { "Already deleting" }
        return@launch
      }

      state.working.value = true
      interactor
          .delete(item)
          .onFailure { Timber.e(it) { "Failed to delete item: $item" } }
          .onSuccess { deleted ->
            if (deleted) {
              Timber.d { "Transaction item: $item" }
              state.item.value = null
              onDeleted(item)
            } else {
              Timber.w { "Item was not deleted: $item" }
            }
          }
          .onFinally { state.working.value = false }
    }
  }
}
