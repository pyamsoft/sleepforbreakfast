/*
 * Copyright 2025 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.money.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.core.cast
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractor
import com.pyamsoft.sleepforbreakfast.money.one.OneViewModeler
import java.math.BigDecimal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class MoneyAddViewModeler<I : Any, T : Any, S : MutableMoneyAddViewState>
protected constructor(
    state: S,
    initialId: I,
    private val interactor: ListInteractor<I, T, *>,
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

        registry
            .registerProvider(KEY_CATEGORIES) {
              state.categories.value.joinToString("|") { it.raw }
            }
            .also { add(it) }

        onRegisterSaveState(registry)
      }

  final override fun consumeRestoredState(registry: SaveableStateRegistry) {
    registry.consumeRestored(KEY_NAME)?.cast<String>()?.also { state.name.value = it }

    registry.consumeRestored(KEY_NOTE)?.cast<String>()?.also { state.note.value = it }

    registry.consumeRestored(KEY_AMOUNT)?.cast<String>()?.also { state.amount.value = it }

    registry
        .consumeRestored(KEY_CATEGORIES)
        ?.cast<String>()
        ?.split("|")
        ?.map { DbCategory.Id(it) }
        ?.also { state.categories.value = it }

    registry
        .consumeRestored(KEY_TYPE)
        ?.cast<String>()
        ?.let { DbTransaction.Type.valueOf(it) }
        ?.also { state.type.value = it }

    onConsumeRestoredState(registry)
  }

  protected open suspend fun onNewCreated(data: T) {}

  fun handleNameChanged(name: String) {
    state.name.value = name
  }

  fun handleNoteChanged(note: String) {
    state.note.value = note
  }

  fun handleTypeChanged(type: DbTransaction.Type) {
    state.type.value = type
  }

  fun handleAmountChanged(amount: String) {
    state.amount.value = amount
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
      val data: T
      try {
        data = compile()
      } catch (e: Throwable) {
        Timber.e(e) { "Error compiling data" }
        state.working.value = false
        // TODO handle error in UI
        return@launch
      }

      interactor
          .submit(data)
          .onFailure { Timber.e(it) { "Error occurred when submitting data $data" } }
          .onSuccess { res ->
            when (res) {
              is DbInsert.InsertResult.Insert -> {
                Timber.d { "New data: ${res.data}" }
                onNewCreated(res.data)
              }
              is DbInsert.InsertResult.Update -> {
                Timber.d { "Update data: ${res.data}" }
                onNewCreated(res.data)
              }
              is DbInsert.InsertResult.Fail -> {
                Timber.e(res.error) { "Failed to insert data: $data" }

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
            Timber.e(it) { "Unable to process repeat: $data" }
            // TODO handle error in UI
          }
          .onFinally { state.working.value = false }
    }
  }

  @CheckResult protected abstract suspend fun compile(): T

  protected abstract fun MutableList<SaveableStateRegistry.Entry>.onRegisterSaveState(
      registry: SaveableStateRegistry
  )

  protected abstract fun onConsumeRestoredState(registry: SaveableStateRegistry)

  abstract fun handleReset()

  companion object {
    private const val KEY_NAME = "key_name"
    private const val KEY_NOTE = "key_note"
    private const val KEY_TYPE = "key_type"
    private const val KEY_AMOUNT = "key_amount"
    private const val KEY_CATEGORIES = "key_categories"

    private val ONE_HUNDRED = BigDecimal.valueOf(100L)

    /** Cents is a long, parse it to a Double and then to a string */
    @JvmStatic
    @CheckResult
    protected fun Long.toAmount(orElse: (() -> String)? = null): String {
      try {
        if (this < 0) {
          throw IllegalArgumentException("Cents cannot be negative: $this")
        }

        // Converts -> 15049 ==> 150.48
        return BigDecimal.valueOf(this, 2).toPlainString()
      } catch (e: Throwable) {
        // For whatever reason we have failed

        // Either return a default
        if (orElse != null) {
          return orElse()
        }

        // Or throw
        throw e
      }
    }

    /** Convert a string to a cents amount */
    @JvmStatic
    @CheckResult
    protected fun String.toCents(orElse: (() -> Long)? = null): Long {
      try {
        val plainNumberLike = this.removePrefix("$")

        // Guard negative
        val raw = BigDecimal(plainNumberLike).multiply(ONE_HUNDRED).longValueExact()
        if (raw < 0) {
          throw IllegalArgumentException("Amount string cannot be negative: $this => $raw")
        }

        return raw
      } catch (e: Throwable) {
        // For whatever reason we have failed

        // Either return a default
        if (orElse != null) {
          return orElse()
        }

        // Or throw
        throw e
      }
    }
  }
}
