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

package com.pyamsoft.sleepforbreakfast.sources

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.sources.add.SourcesAddParams
import com.pyamsoft.sleepforbreakfast.sources.delete.SourcesDeleteParams
import com.pyamsoft.sleepforbreakfast.ui.savedstate.JsonParser
import com.pyamsoft.sleepforbreakfast.ui.savedstate.fromJson
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope

class SourcesViewModeler
@Inject
internal constructor(
    override val state: MutableSourcesViewState,
    private val jsonParser: JsonParser,
) : AbstractViewModeler<SourcesViewState>(state) {

  private fun handleAddParams(params: SourcesAddParams) {
    state.addParams.value = params
  }

  private fun handleDeleteParams(params: SourcesDeleteParams) {
    state.deleteParams.value = params
  }

  //  private fun listenForTransactions(scope: CoroutineScope) {
  //    scope.launch(context = Dispatchers.Main) {
  //      interactor.listenToTransactions { event ->
  //        when (event) {
  //          is TransactionChangeEvent.Delete ->
  //              // When actually deleted from the DB, offer undo ability
  //              handleTransactionDeleted(event.transaction, offerUndo = true)
  //          is TransactionChangeEvent.Insert -> handleTransactionInserted(event.transaction)
  //          is TransactionChangeEvent.Update -> handleTransactionUpdated(event.transaction)
  //        }
  //      }
  //    }
  //  }
  //
  //  private fun handleTransactionDeleted(
  //      transaction: DbTransaction,
  //      offerUndo: Boolean,
  //  ) {
  //    val s = state
  //    s.transactions.update { list ->
  //      list.filterNot { it.id == transaction.id }.sortedByDescending { it.date }
  //    }
  //
  //    if (offerUndo) {
  //      Timber.d("Offer undo on transaction delete: $transaction")
  //      s.recentlyDeleteTransaction.value = transaction
  //    }
  //  }
  //
  //  private fun handleTransactionUpdated(transaction: DbTransaction) {
  //    state.transactions.update { list ->
  //      list.map { if (it.id == transaction.id) transaction else it }.sortedByDescending { it.date
  // }
  //    }
  //  }
  //
  //  private fun handleTransactionInserted(transaction: DbTransaction) {
  //    state.transactions.update { list -> (list + transaction).sortedByDescending { it.date } }
  //  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
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

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    registry
        .consumeRestored(KEY_ADD_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<SourcesAddParams.Json>(it) }
        ?.fromJson()
        ?.let { state.addParams.value = it }

    registry
        .consumeRestored(KEY_DELETE_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<SourcesDeleteParams.Json>(it) }
        ?.fromJson()
        ?.let { state.deleteParams.value = it }
  }

  fun bind(scope: CoroutineScope) {
    //    handleRefresh(
    //        scope = scope,
    //        force = false,
    //    )
    //
    //    listenForTransactions(scope = scope)
  }

  //  fun handleRefresh(scope: CoroutineScope, force: Boolean) {
  //    if (state.loadingState.value == TransactionViewState.LoadingState.LOADING) {
  //      Timber.w("Already loading transaction list")
  //      return
  //    }
  //
  //    scope.launch(context = Dispatchers.Main) {
  //      if (state.loadingState.value == TransactionViewState.LoadingState.LOADING) {
  //        Timber.w("Already loading transaction list")
  //        return@launch
  //      }
  //
  //      state.loadingState.value = TransactionViewState.LoadingState.LOADING
  //      interactor
  //          .loadAll(force = force)
  //          .map { list -> list.sortedByDescending { it.date } }
  //          .onSuccess { Timber.d("Loaded transaction list: $it") }
  //          .onSuccess { transactions ->
  //            state.transactions.value = transactions
  //            state.transactionError.value = null
  //          }
  //          .onFailure { Timber.e(it, "Error loading transactions") }
  //          .onFailure { err ->
  //            state.transactions.value = emptyList()
  //            state.transactionError.value = err
  //          }
  //          .onFinally { state.loadingState.value = TransactionViewState.LoadingState.DONE }
  //    }
  //  }

  fun handleEditSources(source: DbSource) {
    handleAddParams(
        params =
            SourcesAddParams(
                sourcesId = source.id,
            ),
    )
  }

  fun handleAddNewSources() {
    handleAddParams(
        params =
            SourcesAddParams(
                sourcesId = DbSource.Id.EMPTY,
            ),
    )
  }

  fun handleCloseAddSources() {
    state.addParams.value = null
  }

  //  fun handleTransactionDeleteFinal() {
  //    deleteRestoreHandler.handleDeleteFinal(state.recentlyDeleteTransaction) {
  //      // Once finally deleted, don't offer undo
  //      handleTransactionDeleted(it, offerUndo = false)
  //    }
  //  }
  //
  //  fun handleRestoreDeletedTransaction(scope: CoroutineScope) {
  //    deleteRestoreHandler.handleRestoreDeleted(
  //        scope = scope,
  //        recentlyDeleted = state.recentlyDeleteTransaction,
  //    ) {
  //      interactor.restoreTransaction(it)
  //    }
  //  }
  //
  //  fun handleDeleteTransaction(transaction: DbTransaction) {
  //    handleDeleteParams(
  //        params =
  //            TransactionDeleteParams(
  //                transactionId = transaction.id,
  //            ),
  //    )
  //  }
  //
  //  fun handleCloseDeleteTransaction() {
  //    state.deleteParams.value = null
  //  }

  companion object {
    private const val KEY_ADD_PARAMS = "key_add_params"
    private const val KEY_DELETE_PARAMS = "key_delete_params"
  }
}
