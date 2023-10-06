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

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.util.contains
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionChangeEvent
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import com.pyamsoft.sleepforbreakfast.money.list.ListViewModeler
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddParams
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteParams
import com.pyamsoft.sleepforbreakfast.transactions.list.BreakdownRange
import com.pyamsoft.sleepforbreakfast.ui.savedstate.JsonParser
import com.pyamsoft.sleepforbreakfast.ui.savedstate.fromJson
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TransactionViewModeler
@Inject
internal constructor(
    state: MutableTransactionViewState,
    interactor: TransactionInteractor,
    private val enforcer: ThreadEnforcer,
    private val jsonParser: JsonParser,
    private val categoryLoader: CategoryLoader,
    private val defaultCategoryId: DbCategory.Id,
) :
    TransactionViewState by state,
    ListViewModeler<DbTransaction, TransactionChangeEvent, MutableTransactionViewState>(
        enforcer = enforcer,
        state = state,
        interactor = interactor,
    ) {

  private fun handleAddParams(params: TransactionAddParams) {
    state.addParams.value = params
  }

  private fun handleDeleteParams(params: TransactionDeleteParams) {
    state.deleteParams.value = params
  }

  @CheckResult
  private suspend fun loadTargetCategory(): DbCategory {
    val knownCategory = state.category.value
    return if (knownCategory == null) {
      val category = categoryLoader.queryAll().firstOrNull { it.id == defaultCategoryId }
      state.category.value = category
      category ?: DbCategory.NONE
    } else {
      knownCategory
    }
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

    registry.registerProvider(KEY_IS_CHART_OPEN) { state.isChartOpen.value }.also { add(it) }
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
        .consumeRestored(KEY_IS_CHART_OPEN)
        ?.let { it as Boolean }
        ?.also { state.isChartOpen.value = it }

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

  override fun onGenerateItemsBasedOnAllItems(
      scope: CoroutineScope,
      allItems: StateFlow<List<DbTransaction>>
  ) {
    // Create a source that generates data based on the latest from all sources
    val combined =
        combineTransform(
                allItems,
                state.search,
                state.breakdown,
            ) { all, search, breakdown ->
              enforcer.assertOffMainThread()

              emit(
                  ItemPayload(
                      transactions = all,
                      search = search,
                      range = breakdown,
                  ),
              )
            }
            // Enforcee in background
            .flowOn(context = Dispatchers.Default)

    scope.launch(context = Dispatchers.Default) {
      combined.collect { (all, search, range) ->
        enforcer.assertOffMainThread()

        val category = loadTargetCategory()

        state.items.value =
            all.asSequence()
                // The NONE category captures everything
                .filter { t ->
                  if (category.id.isEmpty) {
                    // Either no categories
                    return@filter t.categories.isEmpty() ||
                        // Or one category, where the category is empty
                        t.categories.size == 1 && t.categories.first().isEmpty
                  } else {
                    // This includes the category in its list of categories
                    return@filter t.categories.contains { it == category.id }
                  }
                }
                // Filter by search query
                .run {
                  if (search.isNotBlank()) {
                    filter { isMatchingSearch(it, search) }
                  } else {
                    this
                  }
                }
                // Filter by search query
                .run {
                  if (range != null) {
                    filter { it.date.toLocalDate().let { d -> d >= range.start && d <= range.end } }
                  } else {
                    this
                  }
                }
                .toList()
                .sort()
      }
    }
  }

  fun handleEditTransaction(transaction: DbTransaction) {
    handleAddParams(
        params =
            TransactionAddParams(
                transactionId = transaction.id,
                ensureCategoryId = defaultCategoryId,
            ),
    )
  }

  fun handleAddNewTransaction() {
    handleAddParams(
        params =
            TransactionAddParams(
                transactionId = DbTransaction.Id.EMPTY,
                ensureCategoryId = defaultCategoryId,
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

  fun handleToggleChart() {
    state.isChartOpen.update { !it }
  }

  private data class ItemPayload(
      val transactions: List<DbTransaction>,
      val search: String,
      val range: BreakdownRange?,
  )

  companion object {
    private const val KEY_ADD_PARAMS = "key_add_params"
    private const val KEY_DELETE_PARAMS = "key_delete_params"

    private const val KEY_IS_BREAKDOWN_OPEN = "key_is_breakdown"
    private const val KEY_BREAKDOWN_RANGE = "key_breakdown_range"

    private const val KEY_IS_CHART_OPEN = "key_is_chart"
  }
}
