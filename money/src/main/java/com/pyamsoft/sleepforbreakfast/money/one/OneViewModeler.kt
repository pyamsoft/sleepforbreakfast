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

package com.pyamsoft.sleepforbreakfast.money.one

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class OneViewModeler<I : Any, T : Any, S : UiViewState>
protected constructor(
    final override val state: S,
    protected val initialId: I,
    private val interactor: ListInteractor<I, T, *>,
) : AbstractViewModeler<S>(state) {

  fun bind(scope: CoroutineScope, force: Boolean) {
    // Upon binding, load the existing
    if (!isIdEmpty(initialId)) {
      scope.launch(context = Dispatchers.Default) {
        interactor
            .loadOne(force, initialId)
            .onSuccess { result ->
              Timber.d("Loaded data: $result")
              onDataLoaded(result)
            }
            .onFailure { Timber.e(it, "Error loading data: $initialId") }
      }
    }

    onBind(scope = scope)
  }

  protected abstract fun onBind(scope: CoroutineScope)

  protected abstract fun CoroutineScope.onDataLoaded(result: T)

  @CheckResult protected abstract fun isIdEmpty(id: I): Boolean
}
