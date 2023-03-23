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

package com.pyamsoft.sleepforbreakfast.sources.add

import androidx.annotation.CheckResult
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.money.one.OneViewModeler
import com.pyamsoft.sleepforbreakfast.sources.SourcesInteractor
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class SourcesAddViewModeler
@Inject
internal constructor(
    state: MutableSourcesAddViewState,
    params: SourcesAddParams,
    interactor: SourcesInteractor,
    private val clock: Clock,
    private val addInteractor: SourceAddInteractor,
) :
    OneViewModeler<DbSource.Id, DbSource, MutableSourcesAddViewState>(
        state = state,
        initialId = params.sourcesId,
        interactor = interactor,
    ) {

  private val submitRunner =
      highlander<ResultWrapper<DbInsert.InsertResult<DbSource>>, DbSource> { source ->
        addInteractor.submit(source)
      }

  @CheckResult
  private fun compile(): DbSource {
    return DbSource.create(clock, initialId)
        .name(state.name.value)
        .accountNumber(state.accountNumber.value)
        .note(state.note.value)
  }
  override fun onBind(scope: CoroutineScope) {
    handleReset()
  }

  override fun isIdEmpty(id: DbSource.Id): Boolean {
    return id.isEmpty
  }

  override fun onDataLoaded(result: DbSource) {
    handleReset(result)
  }

  fun handleReset(s: DbSource? = null) {
    if (s == null) {
      state.name.value = ""
      state.accountNumber.value = ""
      state.note.value = ""
    } else {
      state.name.value = s.name
      state.accountNumber.value = s.accountNumber
      state.note.value = s.note
    }
  }

  fun handleNameChanged(name: String) {
    state.name.value = name
  }

  fun handleNoteChanged(note: String) {
    state.note.value = note
  }

  fun handleAccountNumberChanged(num: String) {
    state.accountNumber.value = num
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

      val source = compile()
      state.working.value = true
      submitRunner
          .call(source)
          .onFailure { Timber.e(it, "Error occurred when submitting source $source") }
          .onSuccess { res ->
            when (res) {
              is DbInsert.InsertResult.Insert -> Timber.d("New source: ${res.data}")
              is DbInsert.InsertResult.Update -> Timber.d("Update source: ${res.data}")
              is DbInsert.InsertResult.Fail -> {
                Timber.e(res.error, "Failed to insert source: $source")

                // Will be caught by onFailure below
                throw res.error
              }
            }
          }
          .onFailure {
            Timber.e(it, "Unable to process source: $source")
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
}
