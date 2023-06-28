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

package com.pyamsoft.sleepforbreakfast.repeat.add

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.replaceTransactionCategories
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.add.MoneyAddViewModeler
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import com.pyamsoft.sleepforbreakfast.repeat.RepeatInteractor
import com.pyamsoft.sleepforbreakfast.worker.WorkJobType
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber

class RepeatAddViewModeler
@Inject
internal constructor(
    state: MutableRepeatAddViewState,
    interactor: RepeatInteractor,
    params: RepeatAddParams,
    private val clock: Clock,
    private val categoryLoader: CategoryLoader,
    private val workerQueue: WorkerQueue,
) :
    RepeatAddViewState by state,
    MoneyAddViewModeler<DbRepeat.Id, DbRepeat, MutableRepeatAddViewState>(
        state = state,
        initialId = params.repeatId,
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

  override fun isIdEmpty(id: DbRepeat.Id): Boolean {
    return id.isEmpty
  }

  override fun onBind(scope: CoroutineScope) {
    // Clear everything
    handleReset()

    scope.launch(context = Dispatchers.Default) { loadCategories() }
  }

  override fun CoroutineScope.onDataLoaded(result: DbRepeat) {
    state.existingRepeat.value = result

    // But once we are loaded initialize everything
    handleReset()
  }

  override fun onConsumeRestoredState(registry: SaveableStateRegistry) {
    registry
        .consumeRestored(KEY_FIRST_DAY)
        ?.let { it as String }
        ?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
        ?.also { state.repeatFirstDay.value = it }

    registry
        .consumeRestored(KEY_REPEAT_TYPE)
        ?.let { it as String }
        ?.let { DbRepeat.Type.valueOf(it) }
        ?.also { state.repeatType.value = it }

    registry
        .consumeRestored(KEY_DATE_DIALOG)
        ?.let { it as Boolean }
        ?.also { state.isDateDialogOpen.value = it }
  }

  override fun MutableList<SaveableStateRegistry.Entry>.onRegisterSaveState(
      registry: SaveableStateRegistry
  ) {
    registry
        .registerProvider(KEY_FIRST_DAY) {
          DateTimeFormatter.ISO_LOCAL_DATE.format(state.repeatFirstDay.value)
        }
        .also { add(it) }

    registry.registerProvider(KEY_REPEAT_TYPE) { state.repeatType.value.name }.also { add(it) }

    registry.registerProvider(KEY_DATE_DIALOG) { state.isDateDialogOpen.value }.also { add(it) }
  }

  override fun handleReset() {
    val source = state.existingRepeat.value

    if (source == null) {
      state.name.value = ""
      state.note.value = ""
      state.type.value = DbTransaction.Type.SPEND
      state.amount.value = 0L
      state.categories.value = emptyList()

      state.repeatFirstDay.value = LocalDate.now(clock)
      state.repeatType.value = DbRepeat.Type.DAILY
    } else {
      state.name.value = source.transactionName
      state.note.value = source.transactionNote
      state.type.value = source.transactionType
      state.amount.value = source.transactionAmountInCents
      state.categories.value = source.transactionCategories

      state.repeatFirstDay.value = source.firstDay
      state.repeatType.value = source.repeatType
    }

    handleCloseDateDialog()
  }

  override suspend fun compile(): DbRepeat {
    val repeat = state.existingRepeat.value ?: DbRepeat.create(clock, initialId)
    return repeat
        .repeatType(state.repeatType.value)
        .firstDay(state.repeatFirstDay.value)
        .transactionName(state.name.value)
        .transactionAmountInCents(state.amount.value)
        .transactionNote(state.note.value)
        .transactionType(state.type.value)
        .replaceTransactionCategories(state.categories.value)
  }

  override suspend fun onNewCreated(data: DbRepeat) {
    workerQueue.enqueue(type = WorkJobType.ONESHOT_CREATE_TRANSACTIONS)
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

  fun handleOpenDateDialog() {
    state.isDateDialogOpen.value = true
  }

  fun handleCloseDateDialog() {
    state.isDateDialogOpen.value = false
  }

  fun handleRepeatTypeChanged(type: DbRepeat.Type) {
    state.repeatType.value = type
  }

  fun handleDateChanged(date: LocalDate) {
    state.repeatFirstDay.update {
      it.withYear(date.year).withMonth(date.monthValue).withDayOfMonth(date.dayOfMonth)
    }
  }

  companion object {
    private const val KEY_DATE_DIALOG = "key_date_dialog"
    private const val KEY_FIRST_DAY = "key_repeat_first_date"
    private const val KEY_REPEAT_TYPE = "key_repeat_type"
  }
}
