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

package com.pyamsoft.sleepforbreakfast.main

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.sleepforbreakfast.core.ActivityScope
import javax.inject.Inject

@ActivityScope
class MainViewModeler
@Inject
internal constructor(
    override val state: MutableMainViewState,
) : AbstractViewModeler<MainViewState>(state) {

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry.registerProvider(KEY_SETTINGS) { s.isSettingsOpen.value }.also { add(it) }
        registry.registerProvider(KEY_TRANSACTIONS) { s.isTransactionsOpen.value }.also { add(it) }
        registry.registerProvider(KEY_REPEATS) { s.isRepeatOpen.value }.also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry
        .consumeRestored(KEY_SETTINGS)
        ?.let { it as Boolean }
        ?.also { s.isSettingsOpen.value = it }

    registry
        .consumeRestored(KEY_TRANSACTIONS)
        ?.let { it as Boolean }
        ?.also { s.isTransactionsOpen.value = it }

    registry.consumeRestored(KEY_REPEATS)?.let { it as Boolean }?.also { s.isRepeatOpen.value = it }
  }

  fun handleOpenSettings() {
    state.isSettingsOpen.value = true
  }

  fun handleCloseSettings() {
    state.isSettingsOpen.value = false
  }

  fun handleOpenTransactions() {
    state.isTransactionsOpen.value = true
  }

  fun handleCloseTransactions() {
    state.isTransactionsOpen.value = false
  }

  fun handleOpenRepeats() {
    state.isRepeatOpen.value = true
  }

  fun handleCloseRepeats() {
    state.isRepeatOpen.value = false
  }

  companion object {

    private const val KEY_SETTINGS = "is_settings_open"
    private const val KEY_TRANSACTIONS = "is_transactions_open"
    private const val KEY_REPEATS = "is_repeats_open"
  }
}
