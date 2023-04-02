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

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface TransactionAutoViewState : UiViewState {
  val loading: StateFlow<LoadingState>
  val auto: StateFlow<DbAutomatic?>
  val autoError: StateFlow<Throwable?>
}

@Stable
class MutableTransactionAutoViewState @Inject internal constructor() : TransactionAutoViewState {

  override val loading = MutableStateFlow(LoadingState.NONE)
  override val auto = MutableStateFlow<DbAutomatic?>(null)
  override val autoError = MutableStateFlow<Throwable?>(null)
}
