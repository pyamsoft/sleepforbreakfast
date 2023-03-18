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

import androidx.annotation.CheckResult
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.replaceCategories
import com.pyamsoft.sleepforbreakfast.money.MoneyViewModeler
import com.pyamsoft.sleepforbreakfast.transactions.base.SingleTransactionHandler
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class TransactionAddViewModeler
@Inject
internal constructor(
    state: MutableTransactionAddViewState,
    private val clock: Clock,
    private val params: TransactionAddParams,
    private val interactor: TransactionAddInteractor,
    private val singleTransactionHandler: SingleTransactionHandler,
) :
    MoneyViewModeler<MutableTransactionAddViewState>(
        state,
        clock,
    ) {

  private val submitRunner =
      highlander<ResultWrapper<DbInsert.InsertResult<DbTransaction>>, DbTransaction> { transaction
        ->
        interactor.submit(transaction)
      }

  @CheckResult
  private fun compile(): DbTransaction {
    return DbTransaction.create(clock, params.transactionId)
        .name(state.name.value)
        .amountInCents(state.amount.value)
        .date(state.date.value)
        .note(state.note.value)
        .type(state.type.value)
        .run {
          state.source.value.let { sid ->
            if (sid == null) {
              removeSourceId()
            } else {
              sourceId(sid)
            }
          }
        }
        .replaceCategories(state.categories.value)
  }

  override fun onBind(scope: CoroutineScope) {
    // Upon opening, load up with this Transaction
    singleTransactionHandler.loadExistingTransaction(
        scope = scope,
        transactionId = params.transactionId,
    ) {
      handleReset(it)
    }
  }

  override suspend fun onSubmitResult(): ResultWrapper<*> {
    val transaction = compile()
    return submitRunner
        .call(transaction)
        .onFailure { Timber.e(it, "Error occurred when submitting transaction $transaction") }
        .onSuccess { res ->
          when (res) {
            is DbInsert.InsertResult.Insert -> Timber.d("New transaction: ${res.data}")
            is DbInsert.InsertResult.Update -> Timber.d("Update transaction: ${res.data}")
            is DbInsert.InsertResult.Fail -> {
              Timber.e(res.error, "Failed to insert transaction: $transaction")

              // Will be caught by onFailure below
              throw res.error
            }
          }
        }
        .onFailure {
          Timber.e(it, "Unable to process transaction: $transaction")
          // TODO handle error in UI
        }
  }
}
