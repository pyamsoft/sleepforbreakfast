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
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.replaceCategories
import com.pyamsoft.sleepforbreakfast.money.MoneyViewModeler
import com.pyamsoft.sleepforbreakfast.money.helper.LoadExistingHandler
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update
import timber.log.Timber

class TransactionAddViewModeler
@Inject
internal constructor(
    state: MutableTransactionAddViewState,
    private val clock: Clock,
    private val params: TransactionAddParams,
    private val interactor: TransactionAddInteractor,
    private val loadTransactionHandler: LoadExistingHandler<DbTransaction.Id, DbTransaction>,
) : MoneyViewModeler<MutableTransactionAddViewState>(state) {

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
    loadTransactionHandler.loadExisting(
        scope = scope,
        id = params.transactionId,
    ) {
      handleReset(ResetPayload.Transaction(it))
    }
  }

  override fun onConsumeRestoredState(registry: SaveableStateRegistry) {
    registry
        .consumeRestored(KEY_DATE)
        ?.let { it as String }
        ?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
        ?.also { state.date.value = it }

    registry
        .consumeRestored(KEY_DATE_DIALOG)
        ?.let { it as Boolean }
        ?.also { state.isDateDialogOpen.value = it }

    registry
        .consumeRestored(KEY_TIME_DIALOG)
        ?.let { it as Boolean }
        ?.also { state.isTimeDialogOpen.value = it }
  }

  override fun MutableList<SaveableStateRegistry.Entry>.onRegisterSaveState(
      registry: SaveableStateRegistry
  ) {
    registry
        .registerProvider(KEY_DATE) {
          DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(state.date.value)
        }
        .also { add(it) }

    registry.registerProvider(KEY_DATE_DIALOG) { state.isDateDialogOpen.value }.also { add(it) }

    registry.registerProvider(KEY_TIME_DIALOG) { state.isTimeDialogOpen.value }.also { add(it) }
  }

  override fun onReset(payload: ResetPayload?) {
    if (payload == null) {
      state.date.value = LocalDateTime.now(clock)
    } else if (payload is ResetPayload.Transaction) {
      state.date.value = payload.transaction.date
    }

    handleCloseDateDialog()
    handleCloseTimeDialog()
  }

  fun handleOpenDateDialog() {
    state.isDateDialogOpen.value = true
  }

  fun handleCloseDateDialog() {
    state.isDateDialogOpen.value = false
  }

  fun handleOpenTimeDialog() {
    state.isTimeDialogOpen.value = true
  }

  fun handleCloseTimeDialog() {
    state.isTimeDialogOpen.value = false
  }

  fun handleDateChanged(date: LocalDate) {
    state.date.update {
      it.withYear(date.year).withMonth(date.monthValue).withDayOfMonth(date.dayOfMonth)
    }
  }

  fun handleTimeChanged(time: LocalTime) {
    state.date.update { it.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0) }
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

  companion object {
    private const val KEY_DATE_DIALOG = "key_date_dialog"
    private const val KEY_TIME_DIALOG = "key_time_dialog"
    private const val KEY_DATE = "key_date"
  }
}
