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

package com.pyamsoft.sleepforbreakfast.transactions.add

import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.add.MoneyAddViewState
import com.pyamsoft.sleepforbreakfast.money.add.MutableMoneyAddViewState
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface TransactionAddViewState : MoneyAddViewState {
  val allCategories: StateFlow<List<DbCategory>>
  val date: StateFlow<LocalDateTime>
  val isDateDialogOpen: StateFlow<Boolean>
  val isTimeDialogOpen: StateFlow<Boolean>

  val existingTransaction: StateFlow<DbTransaction?>

  val loadingRepeat: StateFlow<LoadingState>
  val existingRepeat: StateFlow<DbRepeat?>
  val isRepeatOpen: StateFlow<Boolean>

  val loadingAuto: StateFlow<LoadingState>
  val existingAuto: StateFlow<DbAutomatic?>
  val isAutoOpen: StateFlow<Boolean>
}

@Stable
class MutableTransactionAddViewState
@Inject
internal constructor(
    clock: Clock,
) : TransactionAddViewState, MutableMoneyAddViewState() {

  override val allCategories = MutableStateFlow<List<DbCategory>>(emptyList())
  override val date = MutableStateFlow<LocalDateTime>(LocalDateTime.now(clock))

  override val isDateDialogOpen = MutableStateFlow(false)
  override val isTimeDialogOpen = MutableStateFlow(false)

  override val existingTransaction = MutableStateFlow<DbTransaction?>(null)

  override val loadingRepeat = MutableStateFlow(LoadingState.NONE)
  override val existingRepeat = MutableStateFlow<DbRepeat?>(null)
  override val isRepeatOpen = MutableStateFlow(false)

  override val loadingAuto = MutableStateFlow(LoadingState.NONE)
  override val existingAuto = MutableStateFlow<DbAutomatic?>(null)
  override val isAutoOpen = MutableStateFlow(false)
}
