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

package com.pyamsoft.sleepforbreakfast.transactions.delete

import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.helper.LoadExistingHandler
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class TransactionDeleteViewModeler
@Inject
internal constructor(
    override val state: MutableTransactionDeleteViewState,
    private val params: TransactionDeleteParams,
    private val interactor: TransactionDeleteInteractor,
    private val loadTransactionHandler: LoadExistingHandler<DbTransaction.Id, DbTransaction>,
) : AbstractViewModeler<TransactionDeleteViewState>(state) {

  fun bind(scope: CoroutineScope) {
    loadTransactionHandler.loadExisting(
        scope = scope,
        id = params.transactionId,
    ) {
      state.transaction.value = it
    }
  }

  fun handleDeleteTransaction(
      scope: CoroutineScope,
      onDeleted: (DbTransaction) -> Unit,
  ) {
    if (state.working.value) {
      Timber.w("Already deleting")
      return
    }

    val transaction = state.transaction.value
    if (transaction == null) {
      Timber.w("No transaction, cannot delete")
      return
    }

    scope.launch(context = Dispatchers.Main) {
      if (state.working.value) {
        Timber.w("Already deleting")
        return@launch
      }

      state.working.value = true
      interactor
          .delete(transaction)
          .onFailure { Timber.e(it, "Failed to delete transaction: $transaction") }
          .onSuccess { deleted ->
            if (deleted) {
              Timber.d("Transaction deleted: $transaction")
              state.transaction.value = null
              onDeleted(transaction)
            } else {
              Timber.w("Transaction was not deleted: $transaction")
            }
          }
          .onFinally { state.working.value = false }
    }
  }
}
