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
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.replaceCategories
import com.pyamsoft.sleepforbreakfast.money.add.MoneyAddViewModeler
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import com.pyamsoft.sleepforbreakfast.transactions.TransactionInteractor
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
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
    private val interactor: TransactionInteractor,
    private val categoryLoader: CategoryLoader,
    private val clock: Clock,
) :
    TransactionAddViewState by state,
    MoneyAddViewModeler<DbTransaction.Id, DbTransaction, MutableTransactionAddViewState>(
        state = state,
        initialId = params.transactionId,
        interactor = interactor,
    ) {

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

  private suspend fun loadRepeat(transaction: DbTransaction) {
    if (transaction.repeatId == null) {
      state.existingRepeat.value = null
      state.loadingRepeat.value = LoadingState.DONE
      return
    }

    state.loadingRepeat.value = LoadingState.LOADING
    interactor
        .loadRepeat(transaction)
        .onSuccess {
          when (it) {
            is Maybe.Data -> {
              state.existingRepeat.value = it.data
            }
            is Maybe.None -> {
              state.existingRepeat.value = null
            }
          }
        }
        .onFailure { Timber.e(it, "Error getting repeat data") }
        .onFailure { state.existingRepeat.value = null }
        .onFinally { state.loadingRepeat.value = LoadingState.DONE }
  }

  private suspend fun loadAuto(transaction: DbTransaction) {
    if (transaction.automaticId == null) {
      state.existingAuto.value = null
      state.loadingAuto.value = LoadingState.DONE
      return
    }

    state.loadingAuto.value = LoadingState.LOADING
    interactor
        .loadAuto(transaction)
        .onSuccess {
          when (it) {
            is Maybe.Data -> {
              state.existingAuto.value = it.data
            }
            is Maybe.None -> {
              state.existingAuto.value = null
            }
          }
        }
        .onFailure { Timber.e(it, "Error getting auto data") }
        .onFailure { state.existingAuto.value = null }
        .onFinally { state.loadingAuto.value = LoadingState.DONE }
  }

  override suspend fun compile(): DbTransaction {
    val transaction = state.existingTransaction.value ?: DbTransaction.create(clock, initialId)
    return transaction
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

    scope.launch(context = Dispatchers.Default) { loadCategories() }
  }

  override fun CoroutineScope.onDataLoaded(result: DbTransaction) {
    state.existingTransaction.value = result

    handleReset()

    launch(context = Dispatchers.Default) { loadRepeat(result) }
    launch(context = Dispatchers.Default) { loadAuto(result) }
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
        .consumeRestored(KEY_IS_REPEAT_OPEN)
        ?.let { it as Boolean }
        ?.also { state.isRepeatOpen.value = it }

    registry
        .consumeRestored(KEY_IS_AUTO_OPEN)
        ?.let { it as Boolean }
        ?.also { state.isAutoOpen.value = it }
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

    registry.registerProvider(KEY_IS_REPEAT_OPEN) { state.isRepeatOpen.value }.also { add(it) }

    registry.registerProvider(KEY_IS_AUTO_OPEN) { state.isAutoOpen.value }.also { add(it) }
  }

  override fun handleReset() {
    val source = state.existingTransaction.value
    if (source == null) {
      state.name.value = ""
      state.note.value = ""
      state.type.value = DbTransaction.Type.SPEND
      state.amount.value = 0L
      state.categories.value = emptyList()

      state.date.value = LocalDateTime.now(clock)
    } else {
      state.name.value = source.name
      state.note.value = source.note
      state.type.value = source.type
      state.amount.value = source.amountInCents
      state.categories.value = source.categories

      state.date.value = source.date
    }

    handleCloseRepeatInfo()
    handleCloseAutoInfo()
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

  fun handleOpenRepeatInfo() {
    state.isRepeatOpen.value = true
  }

  fun handleCloseRepeatInfo() {
    state.isRepeatOpen.value = false
  }

  fun handleOpenAutoInfo() {
    state.isAutoOpen.value = true
  }

  fun handleCloseAutoInfo() {
    state.isAutoOpen.value = false
  }

  companion object {
    private const val KEY_DATE_DIALOG = "key_date_dialog"
    private const val KEY_TIME_DIALOG = "key_time_dialog"
    private const val KEY_IS_REPEAT_OPEN = "key_is_repeat_open"
    private const val KEY_IS_AUTO_OPEN = "key_is_auto_open"

    private const val KEY_DATE = "key_date"
  }
}
