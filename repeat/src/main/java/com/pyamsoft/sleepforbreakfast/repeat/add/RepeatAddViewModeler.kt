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
import com.pyamsoft.sleepforbreakfast.money.MoneyViewModeler
import com.pyamsoft.sleepforbreakfast.repeat.base.LoadRepeatHandler
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import timber.log.Timber

class RepeatAddViewModeler
@Inject
internal constructor(
    state: MutableRepeatAddViewState,
    private val clock: Clock,
    private val params: RepeatAddParams,
    private val interactor: RepeatAddInteractor,
    private val loadRepeatHandler: LoadRepeatHandler,
) : MoneyViewModeler<MutableRepeatAddViewState>(state) {

  private val submitRunner =
      highlander<ResultWrapper<DbInsert.InsertResult<DbRepeat>>, DbRepeat> { repeat ->
        interactor.submit(repeat)
      }

  @CheckResult
  private fun compile(): DbRepeat {
    return DbRepeat.create(clock, params.repeatId)
  }

  override fun onBind(scope: CoroutineScope) {
    // Upon opening, load up with this Transaction
    loadRepeatHandler.loadExistingRepeat(
        scope = scope,
        repeatId = params.repeatId,
    ) {
      handleReset(ResetPayload.Repeat(it))
    }
  }

  override fun onConsumeRestoredState(registry: SaveableStateRegistry) {
    // TODO
  }

  override fun MutableList<SaveableStateRegistry.Entry>.onRegisterSaveState(
      registry: SaveableStateRegistry
  ) {
    // TODO
  }

  override fun onReset(payload: ResetPayload?) {
    if (payload == null) {
      state.repeatFirstDay.value = LocalDate.now(clock)
      state.repeatTime.value = LocalTime.now(clock)
      state.repeatType.value = DbRepeat.Type.DAILY
    } else if (payload is ResetPayload.Repeat) {
      val repeat = payload.repeat
      state.repeatFirstDay.value = repeat.firstDate
      state.repeatTime.value = repeat.repeatTime
      state.repeatType.value = repeat.repeatType
    }
  }

  override suspend fun onSubmitResult(): ResultWrapper<*> {
    val transaction = compile()
    return submitRunner
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
  }
}
