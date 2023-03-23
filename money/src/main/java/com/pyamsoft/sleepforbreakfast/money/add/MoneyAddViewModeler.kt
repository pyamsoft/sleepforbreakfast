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

package com.pyamsoft.sleepforbreakfast.money.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractor
import com.pyamsoft.sleepforbreakfast.money.one.OneViewModeler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class MoneyAddViewModeler<I : Any, T : Any, S : MutableMoneyAddViewState>
protected constructor(
    state: S,
    initialId: I,
    interactor: ListInteractor<I, T, *>,
) :
    OneViewModeler<I, T, S>(
        state = state,
        initialId = initialId,
        interactor = interactor,
    ) {

  final override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        registry.registerProvider(KEY_NAME) { state.name.value }.also { add(it) }

        registry.registerProvider(KEY_NOTE) { state.note.value }.also { add(it) }

        registry.registerProvider(KEY_TYPE) { state.type.value.name }.also { add(it) }

        registry.registerProvider(KEY_AMOUNT) { state.amount.value }.also { add(it) }

        onRegisterSaveState(registry)
      }

  final override fun consumeRestoredState(registry: SaveableStateRegistry) {
    registry.consumeRestored(KEY_NAME)?.let { it as String }?.also { state.name.value = it }

    registry.consumeRestored(KEY_NOTE)?.let { it as String }?.also { state.note.value = it }

    registry.consumeRestored(KEY_AMOUNT)?.let { it as Long }?.also { state.amount.value = it }

    registry
        .consumeRestored(KEY_TYPE)
        ?.let { it as String }
        ?.let { DbTransaction.Type.valueOf(it) }
        ?.also { state.type.value = it }

    onConsumeRestoredState(registry)
  }

  fun handleNameChanged(name: String) {
    state.name.value = name
  }

  fun handleNoteChanged(note: String) {
    state.note.value = note
  }

  fun handleTypeChanged(type: DbTransaction.Type) {
    state.type.value = type
  }

  fun handleAmountChanged(amount: Long) {
    state.amount.value = amount
  }

  fun handleReset(payload: ResetPayload? = null) {
    onReset(payload)
    if (payload == null) {
      state.name.value = ""
      state.note.value = ""
      state.type.value = DbTransaction.Type.SPEND
      state.amount.value = 0L
      state.categories.value = emptyList()
      state.source.value = null
    } else {
      when (payload) {
        is ResetPayload.Repeat -> {
          val repeat = payload.repeat
          state.name.value = repeat.transactionName
          state.note.value = repeat.transactionNote
          state.type.value = repeat.transactionType
          state.amount.value = repeat.transactionAmountInCents
          state.categories.value = repeat.transactionCategories
          state.source.value = repeat.transactionSourceId
        }
        is ResetPayload.Transaction -> {
          val transaction = payload.transaction
          state.name.value = transaction.name
          state.note.value = transaction.note
          state.type.value = transaction.type
          state.amount.value = transaction.amountInCents
          state.categories.value = transaction.categories
          state.source.value = transaction.sourceId
        }
      }
    }
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

      state.working.value = true
      onSubmitResult()
          .onSuccess {
            handleReset()

            // Run on the UI
            scope.launch(context = Dispatchers.Main) { onSubmit() }
          }
          .onFinally { state.working.value = false }
    }
  }

  protected abstract fun MutableList<SaveableStateRegistry.Entry>.onRegisterSaveState(
      registry: SaveableStateRegistry
  )

  protected abstract fun onConsumeRestoredState(registry: SaveableStateRegistry)

  protected abstract fun onReset(payload: ResetPayload?)

  @CheckResult protected abstract suspend fun onSubmitResult(): ResultWrapper<*>

  sealed class ResetPayload {
    data class Transaction(val transaction: DbTransaction) : ResetPayload()
    data class Repeat(val repeat: DbRepeat) : ResetPayload()
  }

  companion object {
    private const val KEY_NAME = "key_name"
    private const val KEY_NOTE = "key_note"
    private const val KEY_TYPE = "key_type"
    private const val KEY_AMOUNT = "key_amount"
  }
}
