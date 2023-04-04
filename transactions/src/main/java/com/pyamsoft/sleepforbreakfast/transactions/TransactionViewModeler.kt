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

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionChangeEvent
import com.pyamsoft.sleepforbreakfast.money.list.ListViewModeler
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddParams
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteParams
import com.pyamsoft.sleepforbreakfast.transactions.list.BreakdownRange
import com.pyamsoft.sleepforbreakfast.ui.savedstate.JsonParser
import com.pyamsoft.sleepforbreakfast.ui.savedstate.fromJson
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update

class TransactionViewModeler
@Inject
internal constructor(
    state: MutableTransactionViewState,
    interactor: TransactionInteractor,
    private val jsonParser: JsonParser,
) :
    ListViewModeler<DbTransaction, TransactionChangeEvent, MutableTransactionViewState>(
        state = state,
        interactor = interactor,
    ) {

  private fun handleAddParams(params: TransactionAddParams) {
    state.addParams.value = params
  }

  private fun handleDeleteParams(params: TransactionDeleteParams) {
    state.deleteParams.value = params
  }

  override fun MutableList<SaveableStateRegistry.Entry>.onRegisterSaveState(
      registry: SaveableStateRegistry
  ) {
    registry
        .registerProvider(KEY_ADD_PARAMS) {
          state.addParams.value?.let { jsonParser.toJson(it.toJson()) }
        }
        .also { add(it) }

    registry
        .registerProvider(KEY_DELETE_PARAMS) {
          state.deleteParams.value?.let { jsonParser.toJson(it.toJson()) }
        }
        .also { add(it) }

    registry
        .registerProvider(KEY_BREAKDOWN_RANGE) {
          state.breakdown.value?.let { jsonParser.toJson(it.toJson()) }
        }
        .also { add(it) }

    registry
        .registerProvider(KEY_IS_BREAKDOWN_OPEN) { state.isBreakdownOpen.value }
        .also { add(it) }
  }

  override fun onConsumeRestoredState(registry: SaveableStateRegistry) {
    registry
        .consumeRestored(KEY_BREAKDOWN_RANGE)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<BreakdownRange.Json>(it) }
        ?.fromJson()
        ?.also { handleSetBreakdownRange(it) }

    registry
        .consumeRestored(KEY_IS_BREAKDOWN_OPEN)
        ?.let { it as Boolean }
        ?.also { state.isBreakdownOpen.value = it }

    registry
        .consumeRestored(KEY_ADD_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<TransactionAddParams.Json>(it) }
        ?.fromJson()
        ?.also { handleAddParams(it) }

    registry
        .consumeRestored(KEY_DELETE_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<TransactionDeleteParams.Json>(it) }
        ?.fromJson()
        ?.also { handleDeleteParams(it) }
  }

  override fun CoroutineScope.onItemRealtimeEvent(event: TransactionChangeEvent) {
    when (event) {
      // Offer undo for realtime events
      is TransactionChangeEvent.Delete -> handleItemDeleted(event.transaction, offerUndo = true)
      is TransactionChangeEvent.Insert -> handleItemInserted(event.transaction)
      is TransactionChangeEvent.Update -> handleItemUpdated(event.transaction)
    }
  }

  override fun isMatchingSearch(item: DbTransaction, search: String): Boolean {
    return item.name.contains(search, ignoreCase = true)
  }

  override fun isEqual(o1: DbTransaction, o2: DbTransaction): Boolean {
    return o1.id.raw == o2.id.raw
  }

  override fun List<DbTransaction>.sort(): List<DbTransaction> {
    return this.sortedByDescending { it.date }
  }

  fun handleEditTransaction(transaction: DbTransaction) {
    handleAddParams(
        params =
            TransactionAddParams(
                transactionId = transaction.id,
            ),
    )
  }

  fun handleAddNewTransaction() {
    handleAddParams(
        params =
            TransactionAddParams(
                transactionId = DbTransaction.Id.EMPTY,
            ),
    )
  }

  fun handleCloseAddTransaction() {
    state.addParams.value = null
  }

  fun handleDeleteTransaction(transaction: DbTransaction) {
    handleDeleteParams(
        params =
            TransactionDeleteParams(
                transactionId = transaction.id,
            ),
    )
  }

  fun handleCloseDeleteTransaction() {
    state.deleteParams.value = null
  }

  fun handleToggleBreakdown() {
    state.isBreakdownOpen.update { !it }
  }

  fun handleSetBreakdownRange(range: BreakdownRange) {
    state.breakdown.value = range
  }

  fun handleClearBreakdownRange() {
    state.breakdown.value = null
  }

  companion object {
    private const val KEY_ADD_PARAMS = "key_add_params"
    private const val KEY_DELETE_PARAMS = "key_delete_params"

    private const val KEY_IS_BREAKDOWN_OPEN = "key_is_breakdown"
    private const val KEY_BREAKDOWN_RANGE = "key_breakdown_range"
  }
}
