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

package com.pyamsoft.sleepforbreakfast.transactions.add

import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.money.MoneyViewState
import com.pyamsoft.sleepforbreakfast.money.MutableMoneyViewState
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface TransactionAddViewState : MoneyViewState {
  val date: StateFlow<LocalDateTime>
  val isDateDialogOpen: StateFlow<Boolean>
  val isTimeDialogOpen: StateFlow<Boolean>
}

@Stable
class MutableTransactionAddViewState
@Inject
internal constructor(
    clock: Clock,
) : TransactionAddViewState, MutableMoneyViewState() {

  override val date = MutableStateFlow<LocalDateTime>(LocalDateTime.now(clock))
  override val isDateDialogOpen = MutableStateFlow(false)
  override val isTimeDialogOpen = MutableStateFlow(false)
}
