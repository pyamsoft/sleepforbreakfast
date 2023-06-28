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

package com.pyamsoft.sleepforbreakfast.category.add

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.category.CategoryInteractor
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.one.OneViewModeler
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

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

  @CheckResult
  private fun compile(): DbCategory {
    val category = state.existingCategory.value ?: DbCategory.create(clock, initialId)
    return category.name(state.name.value).note(state.note.value)
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
    handleReset(result)
  }

  fun handleReset(s: DbCategory? = null) {
    if (s == null) {
      state.name.value = ""
      state.note.value = ""
    } else {
      state.name.value = s.name
      state.note.value = s.note
    }
  }

  fun handleNameChanged(name: String) {
    state.name.value = name
  }

  fun handleNoteChanged(note: String) {
    state.note.value = note
  }

  fun handleSubmit(
      scope: CoroutineScope,
      onDismissAfterUpdated: () -> Unit,
  ) {
    Timber.d("Attempt new submission")
    if (state.working.value) {
      Timber.w("Already working")
      return
    }

    scope.launch(context = Dispatchers.Default) {
      if (state.working.value) {
        Timber.w("Already working")
        return@launch
      }

      val category = compile()
      state.working.value = true
      interactor
          .submit(category)
          .onFailure { Timber.e(it, "Error occurred when submitting category $category") }
          .onSuccess { res ->
            when (res) {
              is DbInsert.InsertResult.Insert -> Timber.d("New category: ${res.data}")
              is DbInsert.InsertResult.Update -> Timber.d("Update category: ${res.data}")
              is DbInsert.InsertResult.Fail -> {
                Timber.e(res.error, "Failed to insert category: $category")

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
            Timber.e(it, "Unable to process category: $category")
            // TODO handle error in UI
          }
          .onFinally { state.working.value = false }
    }
  }
}
