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

package com.pyamsoft.sleepforbreakfast.transactions

import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.list.ListViewState
import com.pyamsoft.sleepforbreakfast.money.list.MutableListViewState
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddParams
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteParams
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface TransactionViewState : ListViewState<DbTransaction> {
  val addParams: StateFlow<TransactionAddParams?>
  val deleteParams: StateFlow<TransactionDeleteParams?>

}

@Stable
class MutableTransactionViewState @Inject internal constructor() :
    TransactionViewState, MutableListViewState<DbTransaction>() {
  override val addParams = MutableStateFlow<TransactionAddParams?>(null)
  override val deleteParams = MutableStateFlow<TransactionDeleteParams?>(null)
}
