/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.category.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.sleepforbreakfast.category.CategoryInteractor
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.one.OneViewModeler
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryAddViewModeler
@Inject
internal constructor(
    state: MutableCategoryAddViewState,
    params: CategoryAddParams,
    private val interactor: CategoryInteractor,
    private val clock: Clock,
) :
    CategoryAddViewState by state,
    OneViewModeler<DbCategory.Id, DbCategory, MutableCategoryAddViewState>(
        state = state,
        initialId = params.categoryId,
        interactor = interactor,
    ) {

  private val defaultColor = params.categoryColor

  @CheckResult
  private fun compile(): DbCategory {
    val category = state.existingCategory.value ?: DbCategory.create(clock, initialId)
    return category.name(state.name.value).note(state.note.value).color(state.color.value)
  }

  private fun resetData(s: DbCategory? = null) {
    if (s == null) {
      handleNameChanged("")
      handleNoteChanged("")
      handleColorChanged(defaultColor)
    } else {
      handleNameChanged(s.name)
      handleNoteChanged(s.note)
      handleColorChanged(s.color)
    }

    handleCloseColorPicker()
  }

  override fun onBind(scope: CoroutineScope) {
    handleReset()
  }

  override fun isIdEmpty(id: DbCategory.Id): Boolean {
    return id.isEmpty
  }

  override fun CoroutineScope.onDataLoaded(result: DbCategory) {
    state.existingCategory.value = result

    // Setup UI
    resetData(result)
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        registry.registerProvider(KEY_NAME) { state.name.value }.also { add(it) }
        registry.registerProvider(KEY_NOTE) { state.note.value }.also { add(it) }
        registry.registerProvider(KEY_COLOR) { state.color.value }.also { add(it) }

        registry
            .registerProvider(KEY_COLOR_PICKER_OPEN) { state.showColorPicker.value }
            .also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    registry.consumeRestored(KEY_NAME)?.let { it as String }?.also { handleNameChanged(it) }
    registry.consumeRestored(KEY_NOTE)?.let { it as String }?.also { handleNoteChanged(it) }
    registry.consumeRestored(KEY_COLOR)?.let { it as Long }?.also { handleColorChanged(it) }
    registry
        .consumeRestored(KEY_COLOR_PICKER_OPEN)
        ?.let { it as Boolean }
        ?.also {
          if (it) {
            handleOpenColorPicker()
          } else {
            handleCloseColorPicker()
          }
        }
  }

  fun handleReset() {
    resetData(state.existingCategory.value)
  }

  fun handleNameChanged(name: String) {
    state.name.value = name
  }

  fun handleNoteChanged(note: String) {
    state.note.value = note
  }

  fun handleColorChanged(color: Long) {
    state.color.value = color
  }

  fun handleOpenColorPicker() {
    state.showColorPicker.value = true
  }

  fun handleCloseColorPicker() {
    state.showColorPicker.value = false
  }

  fun handleSubmit(
      scope: CoroutineScope,
      onDismissAfterUpdated: () -> Unit,
  ) {
    Timber.d { "Attempt new submission" }
    if (state.working.value) {
      Timber.w { "Already working" }
      return
    }

    scope.launch(context = Dispatchers.Default) {
      if (state.working.value) {
        Timber.w { "Already working" }
        return@launch
      }

      state.working.value = true
      val category: DbCategory
      try {
        category = compile()
      } catch (e: Throwable) {
        Timber.e(e) { "Error compiling category" }
        state.working.value = false
        // TODO handle error in UI
        return@launch
      }

      interactor
          .submit(category)
          .onFailure { Timber.e(it) { "Error occurred when submitting category $category" } }
          .onSuccess { res ->
            when (res) {
              is DbInsert.InsertResult.Insert -> Timber.d { "New category: ${res.data}" }
              is DbInsert.InsertResult.Update -> Timber.d { "Update category: ${res.data}" }
              is DbInsert.InsertResult.Fail -> {
                Timber.e(res.error) { "Failed to insert category: $category" }

                // Will be caught by onFailure below
                throw res.error
              }
            }
          }
          .onSuccess { handleReset() }
          .onSuccess {
            if (!isIdEmpty(initialId)) {
              // Force onto main thread
              withContext(context = Dispatchers.Default) { onDismissAfterUpdated() }
            }
          }
          .onFailure {
            Timber.e(it) { "Unable to process category: $category" }
            // TODO handle error in UI
          }
          .onFinally { state.working.value = false }
    }
  }

  companion object {
    private const val KEY_NAME = "key_name"
    private const val KEY_NOTE = "key_note"
    private const val KEY_COLOR = "key_color"
    private const val KEY_COLOR_PICKER_OPEN = "key_color_picker_open"
  }
}
