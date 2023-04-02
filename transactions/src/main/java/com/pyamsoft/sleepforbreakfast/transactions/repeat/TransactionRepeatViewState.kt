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

package com.pyamsoft.sleepforbreakfast.transactions.repeat

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface TransactionRepeatViewState : UiViewState {
  val loading: StateFlow<LoadingState>
  val repeat: StateFlow<DbRepeat?>
  val repeatError: StateFlow<Throwable?>
}

@Stable
class MutableTransactionRepeatViewState @Inject internal constructor() :
    TransactionRepeatViewState {

  override val loading = MutableStateFlow(LoadingState.NONE)
  override val repeat = MutableStateFlow<DbRepeat?>(null)
  override val repeatError = MutableStateFlow<Throwable?>(null)
}
