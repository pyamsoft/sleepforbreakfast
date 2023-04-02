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

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.replaceCategories
import com.pyamsoft.sleepforbreakfast.money.add.MoneyAddViewModeler
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import com.pyamsoft.sleepforbreakfast.transactions.ExistingAuto
import com.pyamsoft.sleepforbreakfast.transactions.ExistingRepeat
import com.pyamsoft.sleepforbreakfast.transactions.TransactionInteractor
import com.pyamsoft.sleepforbreakfast.transactions.auto.TransactionAutoParams
import com.pyamsoft.sleepforbreakfast.transactions.repeat.TransactionRepeatInfoParams
import com.pyamsoft.sleepforbreakfast.ui.savedstate.JsonParser
import com.pyamsoft.sleepforbreakfast.ui.savedstate.fromJson
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
    state: MutableTransactionAddViewState,
    params: TransactionAddParams,
    interactor: TransactionInteractor,
    private val jsonParser: JsonParser,
    private val categoryLoader: CategoryLoader,
    private val clock: Clock,
) :
    MoneyAddViewModeler<DbTransaction.Id, DbTransaction, MutableTransactionAddViewState>(
        state = state,
        initialId = params.transactionId,
        interactor = interactor,
    ) {

  private fun handleRepeatInfoParams(params: TransactionRepeatInfoParams) {
    state.repeatInfoParams.value = params
  }

  private fun handleAutoParams(params: TransactionAutoParams) {
    state.autoParams.value = params
  }

  private suspend fun loadCategories() {
    categoryLoader
        .queryAllResult()
        .onSuccess { Timber.d("Loaded categories: $it") }
        .onSuccess { cats -> state.allCategories.value = cats.sortedBy { it.name } }
        .onFailure { Timber.e(it, "Error loading all categories") }
        .onFailure { state.allCategories.value = emptyList() }
  }

  @CheckResult
  private fun getOnlyExistingCategories(): List<DbCategory.Id> {
    var cleaned = emptyList<DbCategory.Id>()
    val currentCategories = state.categories.value
    val allCategories = state.allCategories.value
    if (allCategories.isEmpty()) {
      Timber.w("Could not load allCategories, do not change categories for compile()")
      cleaned = currentCategories
    } else {
      // For each category currently added
      for (cat in currentCategories) {
        // Check that it still exists in the DB
        val exists = allCategories.firstOrNull { it.id == cat }

        // If it does great, use it
        if (exists != null) {
          cleaned = cleaned + cat
        } else {
          // Otherwise this "was" a category but it has been deleted
          Timber.w("Category was selected but no longer exists: $cat")
        }
      }
    }

    return cleaned
  }

  override suspend fun compile(): DbTransaction {
    return DbTransaction.create(clock, initialId)
        .name(state.name.value)
        .amountInCents(state.amount.value)
        .date(state.date.value)
        .note(state.note.value)
        .type(state.type.value)
        .replaceCategories(getOnlyExistingCategories())
  }

  override fun isIdEmpty(id: DbTransaction.Id): Boolean {
    return id.isEmpty
  }

  override fun onBind(scope: CoroutineScope) {
    handleReset()

    scope.launch(context = Dispatchers.Main) { loadCategories() }
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

    registry
        .consumeRestored(KEY_REPEAT_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<TransactionRepeatInfoParams.Json>(it) }
        ?.fromJson()
        ?.let { handleRepeatInfoParams(it) }

    registry
        .consumeRestored(KEY_AUTO_PARAMS)
        ?.let { it as String }
        ?.let { jsonParser.fromJson<TransactionAutoParams.Json>(it) }
        ?.fromJson()
        ?.let { handleAutoParams(it) }
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

    registry
        .registerProvider(KEY_REPEAT_PARAMS) {
          state.repeatInfoParams.value?.let { jsonParser.toJson(it.toJson()) }
        }
        .also { add(it) }

    registry
        .registerProvider(KEY_AUTO_PARAMS) {
          state.autoParams.value?.let { jsonParser.toJson(it.toJson()) }
        }
        .also { add(it) }
  }

  override fun onReset(payload: DbTransaction?) {
    if (payload == null) {
      state.date.value = LocalDateTime.now(clock)
      state.existingRepeat.value = null
    } else {
      state.name.value = payload.name
      state.note.value = payload.note
      state.type.value = payload.type
      state.amount.value = payload.amountInCents
      state.categories.value = payload.categories

      val r = payload.repeatId
      val d = payload.repeatCreatedDate
      if (r != null && d != null) {
        state.existingRepeat.value =
            ExistingRepeat(
                id = r,
                date = d,
            )
      } else {
        state.existingRepeat.value = null
      }
      state.date.value = payload.date
    }

    handleCloseRepeatInfoTransaction()
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

  fun handleCategoryAdded(category: DbCategory) {
    state.categories.update { list ->
      if (!list.contains(category.id)) {
        list + category.id
      } else {
        list
      }
    }
  }

  fun handleCategoryRemoved(category: DbCategory) {
    state.categories.update { list -> list.filterNot { it == category.id } }
  }

  fun handleRepeatInfoTransaction(existing: ExistingRepeat) {
    handleRepeatInfoParams(
        params =
            TransactionRepeatInfoParams(
                repeatId = existing.id,
                transactionRepeatDate = existing.date,
            ),
    )
  }

  fun handleCloseRepeatInfoTransaction() {
    state.repeatInfoParams.value = null
  }

  fun handleAutoTransaction(existing: ExistingAuto) {
    handleAutoParams(
        params =
            TransactionAutoParams(
                autoId = existing.id,
                autoDate = existing.date,
            ),
    )
  }

  fun handleCloseAutoTransaction() {
    state.autoParams.value = null
  }

  companion object {
    private const val KEY_DATE_DIALOG = "key_date_dialog"
    private const val KEY_TIME_DIALOG = "key_time_dialog"
    private const val KEY_REPEAT_PARAMS = "key_repeat_params"
    private const val KEY_AUTO_PARAMS = "key_auto_params"

    private const val KEY_DATE = "key_date"
  }
}
