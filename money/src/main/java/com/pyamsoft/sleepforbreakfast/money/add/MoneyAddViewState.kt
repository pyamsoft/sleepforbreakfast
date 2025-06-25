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

package com.pyamsoft.sleepforbreakfast.money.add

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface MoneyAddViewState : UiViewState {
  val name: StateFlow<String>
  val categories: StateFlow<List<DbCategory.Id>>
  val amount: StateFlow<String>
  val type: StateFlow<DbTransaction.Type>
  val note: StateFlow<String>
  val working: StateFlow<Boolean>
}

@Stable
abstract class MutableMoneyAddViewState protected constructor() : MoneyAddViewState {
  final override val name = MutableStateFlow("")
  final override val categories = MutableStateFlow<List<DbCategory.Id>>(emptyList())
  final override val amount = MutableStateFlow("")
  final override val type = MutableStateFlow(DbTransaction.Type.SPEND)
  final override val note = MutableStateFlow("")
  final override val working = MutableStateFlow(false)
}
