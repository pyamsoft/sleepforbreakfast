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
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface TransactionAddViewState : UiViewState {
  val name: StateFlow<String>
  val source: StateFlow<DbSource.Id?>
  val categories: StateFlow<List<DbCategory.Id>>
  val amount: StateFlow<Long>
  val type: StateFlow<DbTransaction.Type>
  val note: StateFlow<String>
  val date: StateFlow<LocalDateTime>

  val isDateDialogOpen: StateFlow<Boolean>
  val isTimeDialogOpen: StateFlow<Boolean>

  val working: StateFlow<Boolean>
}

@Stable
class MutableTransactionAddViewState
@Inject
internal constructor(
    clock: Clock,
) : TransactionAddViewState {
  override val name = MutableStateFlow("")
  override val source = MutableStateFlow<DbSource.Id?>(null)
  override val categories = MutableStateFlow<List<DbCategory.Id>>(emptyList())
  override val amount = MutableStateFlow(0L)
  override val type = MutableStateFlow(DbTransaction.Type.SPEND)
  override val note = MutableStateFlow("")
  override val date = MutableStateFlow<LocalDateTime>(LocalDateTime.now(clock))
  override val isDateDialogOpen = MutableStateFlow(false)
  override val isTimeDialogOpen = MutableStateFlow(false)
  override val working = MutableStateFlow(false)
}
