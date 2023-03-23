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

package com.pyamsoft.sleepforbreakfast.repeat.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.highlander.highlander
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.replaceTransactionCategories
import com.pyamsoft.sleepforbreakfast.money.add.MoneyAddViewModeler
import com.pyamsoft.sleepforbreakfast.repeat.RepeatInteractor
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class RepeatAddViewModeler
@Inject
internal constructor(
    state: MutableRepeatAddViewState,
    interactor: RepeatInteractor,
    params: RepeatAddParams,
    private val clock: Clock,
    private val addInteractor: RepeatAddInteractor,
) :
    MoneyAddViewModeler<DbRepeat.Id, DbRepeat, MutableRepeatAddViewState>(
        state = state,
        initialId = params.repeatId,
        interactor = interactor,
    ) {

  private val submitRunner =
      highlander<ResultWrapper<DbInsert.InsertResult<DbRepeat>>, DbRepeat> { repeat ->
        addInteractor.submit(repeat)
      }

  @CheckResult
  private fun compile(): DbRepeat {
    return DbRepeat.create(clock, initialId)
        .repeatType(state.repeatType.value)
        .firstDay(state.repeatFirstDay.value)
        .unarchive()
        .activate()
        .transactionName(state.name.value)
        .transactionAmountInCents(state.amount.value)
        .transactionNote(state.note.value)
        .transactionType(state.type.value)
        .run {
          state.source.value.let { sid ->
            if (sid == null) {
              removeTransactionSourceId()
            } else {
              transactionSourceId(sid)
            }
          }
        }
        .replaceTransactionCategories(state.categories.value)
  }

  override fun isIdEmpty(id: DbRepeat.Id): Boolean {
    return id.isEmpty
  }

  override fun onBind(scope: CoroutineScope) {
    // Clear everything
    handleReset()
  }

  override fun onDataLoaded(result: DbRepeat) {
    // But once we are loaded initialize everything
    handleReset(ResetPayload.Repeat(result))
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
  }

  override fun onReset(payload: ResetPayload?) {
    if (payload == null) {
      state.repeatFirstDay.value = LocalDate.now(clock)
      state.repeatType.value = DbRepeat.Type.DAILY
    } else if (payload is ResetPayload.Repeat) {
      val repeat = payload.repeat
      state.repeatFirstDay.value = repeat.firstDate
      state.repeatType.value = repeat.repeatType
    }
  }

  override suspend fun onSubmitResult(): ResultWrapper<*> {
    val repeat = compile()
    return submitRunner
        .call(repeat)
        .onFailure { Timber.e(it, "Error occurred when submitting repeat $repeat") }
        .onSuccess { res ->
          when (res) {
            is DbInsert.InsertResult.Insert -> Timber.d("New repeat: ${res.data}")
            is DbInsert.InsertResult.Update -> Timber.d("Update repeat: ${res.data}")
            is DbInsert.InsertResult.Fail -> {
              Timber.e(res.error, "Failed to insert repeat: $repeat")

              // Will be caught by onFailure below
              throw res.error
            }
          }
        }
        .onFailure {
          Timber.e(it, "Unable to process repeat: $repeat")
          // TODO handle error in UI
        }
  }

  companion object {
    private const val KEY_FIRST_DAY = "key_repeat_first_date"
    private const val KEY_REPEAT_TYPE = "key_repeat_type"
  }
}
