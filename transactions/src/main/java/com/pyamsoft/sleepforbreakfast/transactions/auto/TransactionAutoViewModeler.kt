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

package com.pyamsoft.sleepforbreakfast.transactions.auto

import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class TransactionAutoViewModeler
@Inject
internal constructor(
    override val state: MutableTransactionAutoViewState,
    private val params: TransactionAutoParams,
    private val interactor: TransactionAutoInteractor,
) : AbstractViewModeler<TransactionAutoViewState>(state) {

  fun bind(scope: CoroutineScope) {
    scope.launch(context = Dispatchers.Main) {
      if (state.loading.value == LoadingState.LOADING) {
        return@launch
      }

      state.loading.value = LoadingState.LOADING
      interactor
          .getById(params.autoId)
          .onFailure { Timber.e(it, "Failed to load DbAuto for params: $params") }
          .onFailure { err ->
            state.apply {
              auto.value = null
              autoError.value = err
            }
          }
          .onSuccess { r ->
            state.apply {
              auto.value = r
              autoError.value = null
            }
          }
          .onFinally { state.loading.value = LoadingState.DONE }
    }
  }
}
