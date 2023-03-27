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

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.category.system.RequiredCategories
import com.pyamsoft.sleepforbreakfast.db.category.system.SystemCategories
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.replaceTransactionCategories
import com.pyamsoft.sleepforbreakfast.money.add.MoneyAddViewModeler
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import com.pyamsoft.sleepforbreakfast.repeat.RepeatInteractor
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
    private val systemCategories: SystemCategories,
    private val clock: Clock,
    private val categoryLoader: CategoryLoader,
) :
    MoneyAddViewModeler<DbRepeat.Id, DbRepeat, MutableRepeatAddViewState>(
        state = state,
        initialId = params.repeatId,
        interactor = interactor,
    ) {

  private suspend fun loadCategories() {
    categoryLoader
        .queryAllResult()
        .onSuccess { Timber.d("Loaded categories: $it") }
        .onSuccess { state.allCategories.value = it }
        .onFailure { Timber.e(it, "Error loading all categories") }
        .onFailure { state.allCategories.value = emptyList() }
  }

  override fun isIdEmpty(id: DbRepeat.Id): Boolean {
    return id.isEmpty
  }

  override fun onBind(scope: CoroutineScope) {
    // Clear everything
    handleReset()

    scope.launch(context = Dispatchers.Main) { loadCategories() }
  }

  override fun onDataLoaded(result: DbRepeat) {
    // But once we are loaded initialize everything
    handleReset(result)
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

  override fun onReset(payload: DbRepeat?) {
    if (payload == null) {
      state.repeatFirstDay.value = LocalDate.now(clock)
      state.repeatType.value = DbRepeat.Type.DAILY
    } else {
      state.repeatFirstDay.value = payload.firstDate
      state.repeatType.value = payload.repeatType

      state.name.value = payload.transactionName
      state.note.value = payload.transactionNote
      state.type.value = payload.transactionType
      state.amount.value = payload.transactionAmountInCents
      state.categories.value = payload.transactionCategories
    }

    handleCloseDateDialog()
  }

  @CheckResult
  private suspend fun getCategories(): List<DbCategory.Id> {
    val systemCategory = systemCategories.categoryByName(RequiredCategories.REPEATING)
    val cleanCategories = mutableSetOf<DbCategory.Id>()
    if (systemCategory != null) {
      cleanCategories.add(systemCategory.id)
    } else {
      Timber.w("Failed to add system category to repeat")
    }
    for (c in state.categories.value) {
      cleanCategories.add(c)
    }

    return cleanCategories.toList()
  }

  override suspend fun compile(): DbRepeat {
    return DbRepeat.create(clock, initialId)
        .repeatType(state.repeatType.value)
        .firstDay(state.repeatFirstDay.value)
        .unarchive()
        .activate()
        .transactionName(state.name.value)
        .transactionAmountInCents(state.amount.value)
        .transactionNote(state.note.value)
        .transactionType(state.type.value)
        .replaceTransactionCategories(getCategories())
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
