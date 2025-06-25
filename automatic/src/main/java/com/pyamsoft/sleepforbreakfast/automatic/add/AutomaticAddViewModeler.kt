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

package com.pyamsoft.sleepforbreakfast.automatic.add

import androidx.annotation.CheckResult
import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.core.cast
import com.pyamsoft.sleepforbreakfast.automatic.AutomaticInteractor
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotification
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationMatchRegex
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.one.OneViewModeler
import java.time.Clock
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AutomaticAddViewModeler
@Inject
internal constructor(
    state: MutableAutomaticAddViewState,
    params: AutomaticAddParams,
    private val interactor: AutomaticInteractor,
    private val clock: Clock,
) :
    AutomaticAddViewState by state,
    OneViewModeler<DbNotification.Id, DbNotificationWithRegexes, MutableAutomaticAddViewState>(
        state = state,
        initialId = params.notificationId,
        interactor = interactor,
    ) {

  @CheckResult
  private fun compileNotification(): DbNotification {
    val notification =
        state.existingAutomatic.value?.notification
            ?: DbNotification.create(
                clock = clock,
                system = false,
                enabled = true,
                actOnPackageNames = emptySet(),
                name = "",
                type = DbTransaction.Type.SPEND,
            )
    return notification
        .name(state.name.value)
        .actOnPackageName(state.actOnPackageNames.value)
        .type(state.type.value)
        .enabled(state.enabled.value)
        // If we submit the edit, mark the model tainted
        .markTaintedByUser(LocalDateTime.now(clock))
  }

  @CheckResult
  private fun compileMatchRegexes(
      notificationId: DbNotification.Id
  ): Collection<DbNotificationMatchRegex> {
    val existing = state.existingAutomatic.value?.matchRegexes.orEmpty()
    return state.workingRegexes.value.map { r ->
      // If we have an existing regex, update it's text, or make new one
      val exists =
          existing.firstOrNull { it.id.raw == r.id && it.notificationId.raw == notificationId.raw }

      if (exists == null) {
        return@map DbNotificationMatchRegex.create(
            notificationId = notificationId,
            clock = clock,
            text = r.text,
        )
      } else {
        return@map exists.text(r.text)
      }
    }
  }

  @CheckResult
  private fun compile(): DbNotificationWithRegexes {
    val newNotification = compileNotification()
    return DbNotificationWithRegexes.create(
        notification = newNotification,
        regexes = compileMatchRegexes(newNotification.id),
    )
  }

  private fun handleLoadMatchRegexes(regexes: Collection<DbNotificationMatchRegex>) {
    state.workingRegexes.value =
        regexes.map { r ->
          AutomaticAddViewState.BuildMatchRegex(
              id = r.id.raw,
              text = r.text,
          )
        }
  }

  private fun resetData(s: DbNotificationWithRegexes? = null) {
    if (s == null) {
      handleNameChanged("")
      handleTypeChanged(DbTransaction.Type.SPEND)
      handleEnabledChanged(true)
      handleLoadMatchRegexes(emptySet())
    } else {
      val n = s.notification
      handleNameChanged(n.name)
      handleTypeChanged(n.type)
      handleEnabledChanged(n.enabled)
      handleLoadMatchRegexes(s.matchRegexes)
    }
  }

  override fun onBind(scope: CoroutineScope) {
    handleReset()
  }

  override fun isIdEmpty(id: DbNotification.Id): Boolean {
    return id.isEmpty
  }

  override fun CoroutineScope.onDataLoaded(result: DbNotificationWithRegexes) {
    state.existingAutomatic.value = result

    // Setup UI
    resetData(result)
  }

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        registry.registerProvider(KEY_NAME) { state.name.value }.also { add(it) }
        registry.registerProvider(KEY_TYPE) { state.type.value.name }.also { add(it) }
        registry.registerProvider(KEY_ENABLED) { state.enabled.value }.also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    registry.consumeRestored(KEY_NAME)?.cast<String>()?.also { handleNameChanged(it) }

    registry
        .consumeRestored(KEY_TYPE)
        ?.cast<String>()
        ?.let { DbTransaction.Type.valueOf(it) }
        ?.also { handleTypeChanged(it) }

    registry.consumeRestored(KEY_ENABLED)?.cast<Boolean>()?.also { handleEnabledChanged(it) }
  }

  fun handleReset() {
    resetData(state.existingAutomatic.value)
  }

  fun handleNameChanged(name: String) {
    state.name.value = name
  }

  fun handleTypeChanged(type: DbTransaction.Type) {
    state.type.value = type
  }

  fun handleEnabledChanged(enabled: Boolean) {
    state.enabled.value = enabled
  }

  fun handleUpdateMatchRegex(regex: AutomaticAddViewState.BuildMatchRegex) {
    state.workingRegexes.update { r ->
      val existing = r.firstOrNull { it.id == regex.id }
      if (existing == null) {
        return@update r + regex
      } else {
        return@update r.map { if (it.id == regex.id) regex else it }
      }
    }
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
      val notification: DbNotificationWithRegexes
      try {
        notification = compile()
      } catch (e: Throwable) {
        Timber.e(e) { "Error compiling notification" }
        state.working.value = false
        // TODO handle error in UI
        return@launch
      }

      interactor
          .submit(notification)
          .onFailure {
            Timber.e(it) { "Error occurred when submitting notification $notification" }
          }
          .onSuccess { res ->
            when (res) {
              is DbInsert.InsertResult.Insert -> Timber.d { "New notification: ${res.data}" }
              is DbInsert.InsertResult.Update -> Timber.d { "Update notification: ${res.data}" }
              is DbInsert.InsertResult.Fail -> {
                Timber.e(res.error) { "Failed to insert notification: $notification" }

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
            Timber.e(it) { "Unable to process notification: $notification" }
            // TODO handle error in UI
          }
          .onFinally { state.working.value = false }
    }
  }

  companion object {
    private const val KEY_NAME = "key_name"
    private const val KEY_TYPE = "key_type"
    private const val KEY_ENABLED = "key_enabled"
  }
}
