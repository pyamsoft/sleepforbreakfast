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

package com.pyamsoft.sleepforbreakfast.transactions.add

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.replaceCategories
import com.pyamsoft.sleepforbreakfast.money.add.MoneyAddViewModeler
import com.pyamsoft.sleepforbreakfast.transactions.TransactionInteractor
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.update

class TransactionAddViewModeler
@Inject
internal constructor(
    state: MutableTransactionAddViewState,
    interactor: TransactionInteractor,
    params: TransactionAddParams,
    private val clock: Clock,
) :
    MoneyAddViewModeler<DbTransaction.Id, DbTransaction, MutableTransactionAddViewState>(
        state = state,
        initialId = params.transactionId,
        interactor = interactor,
    ) {

  override fun compile(): DbTransaction {
    return DbTransaction.create(clock, initialId)
        .name(state.name.value)
        .amountInCents(state.amount.value)
        .date(state.date.value)
        .note(state.note.value)
        .type(state.type.value)
        .replaceCategories(state.categories.value)
  }

  override fun isIdEmpty(id: DbTransaction.Id): Boolean {
    return id.isEmpty
  }

  override fun onBind(scope: CoroutineScope) {
    handleReset()
  }

  override fun onDataLoaded(result: DbTransaction) {
    handleReset(result)
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

  override fun onReset(payload: DbTransaction?) {
    if (payload == null) {
      state.date.value = LocalDateTime.now(clock)
    } else {
      state.date.value = payload.date

      state.name.value = payload.name
      state.note.value = payload.note
      state.type.value = payload.type
      state.amount.value = payload.amountInCents
      state.categories.value = payload.categories
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

  companion object {
    private const val KEY_DATE_DIALOG = "key_date_dialog"
    private const val KEY_TIME_DIALOG = "key_time_dialog"
    private const val KEY_DATE = "key_date"
  }
}
