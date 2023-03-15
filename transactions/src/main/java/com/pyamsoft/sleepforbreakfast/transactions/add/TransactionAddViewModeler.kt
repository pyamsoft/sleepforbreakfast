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
import com.pyamsoft.sleepforbreakfast.transactions.base.SingleViewModeler
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class TransactionAddViewModeler
@Inject
internal constructor(
    override val state: MutableTransactionAddViewState,
    private val clock: Clock,
    private val params: TransactionAddParams,
    private val interactor: TransactionAddInteractor,
) : SingleViewModeler<TransactionAddViewState>(state, interactor) {

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

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        registry.registerProvider(KEY_NAME) { state.name.value }.also { add(it) }

        registry.registerProvider(KEY_NOTE) { state.note.value }.also { add(it) }

        registry.registerProvider(KEY_TYPE) { state.type.value.name }.also { add(it) }

        registry.registerProvider(KEY_AMOUNT) { state.amount.value }.also { add(it) }

        registry
            .registerProvider(KEY_DATE) { DateTimeFormatter.ISO_DATE_TIME.format(state.date.value) }
            .also { add(it) }

        registry.registerProvider(KEY_DATE_DIALOG) { state.isDateDialogOpen.value }.also { add(it) }
        registry.registerProvider(KEY_TIME_DIALOG) { state.isTimeDialogOpen.value }.also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    registry.consumeRestored(KEY_NAME)?.let { it as String }?.also { state.name.value = it }

    registry.consumeRestored(KEY_NOTE)?.let { it as String }?.also { state.note.value = it }

    registry.consumeRestored(KEY_AMOUNT)?.let { it as Long }?.also { state.amount.value = it }

    registry
        .consumeRestored(KEY_DATE_DIALOG)
        ?.let { it as Boolean }
        ?.also { state.isDateDialogOpen.value = it }
    registry
        .consumeRestored(KEY_TIME_DIALOG)
        ?.let { it as Boolean }
        ?.also { state.isTimeDialogOpen.value = it }

    registry
        .consumeRestored(KEY_TYPE)
        ?.let { it as String }
        ?.let { DbTransaction.Type.valueOf(it) }
        ?.also { state.type.value = it }

    registry
        .consumeRestored(KEY_DATE)
        ?.let { it as String }
        ?.let { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME) }
        ?.also { state.date.value = it }
  }

  fun handleNameChanged(name: String) {
    state.name.value = name
  }

  fun handleNoteChanged(note: String) {
    state.note.value = note
  }

  fun handleTypeChanged(type: DbTransaction.Type) {
    state.type.value = type
  }

  fun handleAmountChanged(amount: Long) {
    state.amount.value = amount
  }

  fun handleDateChanged(date: LocalDate) {
    state.date.update {
      it.withYear(date.year).withMonth(date.monthValue).withDayOfMonth(date.dayOfMonth)
    }
  }

  fun handleTimeChanged(time: LocalTime) {
    state.date.update { it.withHour(time.hour).withMinute(time.minute).withSecond(0).withNano(0) }
  }

  fun handleReset(transaction: DbTransaction? = null) {
    state.name.value = transaction?.name ?: ""
    state.note.value = transaction?.note ?: ""
    state.type.value = transaction?.type ?: DbTransaction.Type.SPEND
    state.date.value = transaction?.date ?: LocalDateTime.now(clock)
    state.amount.value = transaction?.amountInCents ?: 0L
    state.categories.value = transaction?.categories ?: emptyList()
    state.source.value = transaction?.sourceId

    handleCloseDateDialog()
    handleCloseTimeDialog()
  }

  fun bind(scope: CoroutineScope) {
    handleReset()

    // Upon opening, load up with this Transaction
    loadExistingTransaction(
        scope = scope,
        transactionId = params.transactionId,
    ) {
      handleReset(it)
    }
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

  fun handleSubmit(
      scope: CoroutineScope,
      onSubmit: () -> Unit,
  ) {
    Timber.d("Attempt new submission")
    if (state.working.value) {
      Timber.w("Already working")
      return
    }

    scope.launch(context = Dispatchers.Main) {
      if (state.working.value) {
        Timber.w("Already working")
        return@launch
      }

      state.working.value = true

      val transaction = compile()
      submitRunner
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
          .onSuccess {
            handleReset()

            // Run on the UI
            scope.launch(context = Dispatchers.Main) { onSubmit() }
          }
          .onFinally { state.working.value = false }
    }
  }

  companion object {
    private const val KEY_NAME = "key_name"
    private const val KEY_NOTE = "key_note"
    private const val KEY_DATE = "key_date"
    private const val KEY_TYPE = "key_type"
    private const val KEY_AMOUNT = "key_amount"

    private const val KEY_DATE_DIALOG = "key_date_dialog"
    private const val KEY_TIME_DIALOG = "key_time_dialog"
  }
}
