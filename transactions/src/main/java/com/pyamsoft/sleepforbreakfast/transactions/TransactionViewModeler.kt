/*
 * Copyright 2024 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.pydroid.core.cast
import com.pyamsoft.pydroid.util.contains
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionChangeEvent
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import com.pyamsoft.sleepforbreakfast.money.list.ListViewModeler
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddParams
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteParams
import com.pyamsoft.sleepforbreakfast.ui.model.TransactionDateRange
import com.pyamsoft.sleepforbreakfast.ui.savedstate.JsonParser
import com.pyamsoft.sleepforbreakfast.ui.savedstate.fromJson
import java.time.LocalDate
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
    private val defaultCategoryId: DbCategory.Id,
    private val defaultDateRange: TransactionDateRange?,
    private val showAllTransactions: Boolean,
    private val enforcer: ThreadEnforcer,
    private val jsonParser: JsonParser,
    private val categoryLoader: CategoryLoader,
    private val transactionQueryDao: TransactionQueryDao,
    private val transactionQueryCache: TransactionQueryDao.Cache,
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
    return state.category.value
        ?: categoryLoader
            .query()
            .firstOrNull { it.id == defaultCategoryId }
            .let { it ?: DbCategory.NONE }
            .also { state.category.value = it }
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
  }

  override fun onConsumeRestoredState(registry: SaveableStateRegistry) {
    registry
        .consumeRestored(KEY_ADD_PARAMS)
        ?.let { it.cast<String>() }
        ?.let { jsonParser.fromJson<TransactionAddParams.Json>(it) }
        ?.fromJson()
        ?.also { handleAddParams(it) }

    registry
        .consumeRestored(KEY_DELETE_PARAMS)
        ?.let { it.cast<String>() }
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

  override suspend fun loadItems(force: Boolean): ResultWrapper<List<DbTransaction>> {
    val category = defaultCategoryId
    return if (showAllTransactions) super.loadItems(force)
    else {
      // Speed up DB work by only loading relevant category
      try {
        if (force) {
          transactionQueryCache.invalidateByCategory(category)
        }
        val items = transactionQueryDao.queryByCategory(category)
        ResultWrapper.success(items)
      } catch (e: Throwable) {
        ResultWrapper.failure(e)
      }
    }
  }

  @CheckResult
  private fun Sequence<DbTransaction>.filterByCategory(
      category: DbCategory
  ): Sequence<DbTransaction> = filter { t ->
    // If we are showing all transactions, don't filter
    if (showAllTransactions) {
      return@filter true
    }

    // The NONE category captures everything
    if (category.id.isEmpty) {
      // Either no categories
      // Or one category, where the category is empty
      return@filter t.categories.isEmpty() || t.categories.size == 1 && t.categories.first().isEmpty
    }

    // This includes the category in its list of categories
    return@filter t.categories.contains { it == category.id }
  }

  @CheckResult
  private fun Sequence<DbTransaction>.filterBySearch(search: String): Sequence<DbTransaction> =
      filter { t ->
        if (search.isBlank()) {
          return@filter true
        }

        return@filter isMatchingSearch(t, search)
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
                state.dateRange,
            ) { all, search, dateRange ->
              enforcer.assertOffMainThread()

              emit(
                  ItemPayload(
                      transactions = all,
                      search = search,
                      dateRange = dateRange,
                  ),
              )
            }
            // Enforce in background
            .flowOn(context = Dispatchers.Default)

    scope.launch(context = Dispatchers.Default) {
      combined.collect { (all, search, dateRange) ->
        enforcer.assertOffMainThread()

        val category = loadTargetCategory()

        state.items.value =
            all.asSequence()
                .filterByCategory(category)
                .filterByPossibleDateRange(defaultDateRange)
                .filterByPossibleDateRange(dateRange)
                .filterBySearch(search)
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

  fun handleToggleDateRange() {
    state.isDateRangeOpen.update { !it }
  }

  fun handleDateRangeUpdated(from: LocalDate, to: LocalDate) {
    state.dateRange.value =
        TransactionDateRange(
            from = from,
            to = to,
        )
  }

  private data class ItemPayload(
      val transactions: List<DbTransaction>,
      val search: String,
      val dateRange: TransactionDateRange?,
  )

  companion object {
    private const val KEY_ADD_PARAMS = "key_add_params"
    private const val KEY_DELETE_PARAMS = "key_delete_params"

    @CheckResult
    private fun Sequence<DbTransaction>.filterByPossibleDateRange(
        range: TransactionDateRange?
    ): Sequence<DbTransaction> = filter { t ->
      val r = range ?: return@filter true
      return@filter t.date.toLocalDate().let { it >= r.from && it <= r.to }
    }
  }
}
