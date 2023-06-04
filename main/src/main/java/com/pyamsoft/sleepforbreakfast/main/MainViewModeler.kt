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
        registry.registerProvider(KEY_PAGE) { s.page.value?.name }.also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry
        .consumeRestored(KEY_SETTINGS)
        ?.let { it as Boolean }
        ?.also { s.isSettingsOpen.value = it }

    registry
        .consumeRestored(KEY_PAGE)
        ?.let { it as String }
        ?.let { MainPage.valueOf(it) }
        ?.also { s.page.value = it }
  }

  fun handleOpenSettings() {
    state.isSettingsOpen.value = true
  }

  fun handleCloseSettings() {
    state.isSettingsOpen.value = false
  }

  fun handleClosePage() {
    state.page.value = null
  }

  fun handleOpenTransactions() {
    state.page.value = MainPage.TRANSACTION
  }

  fun handleOpenRepeats() {
    state.page.value = MainPage.REPEAT
  }

  fun handleOpenCategory() {
    state.page.value = MainPage.CATEGORY
  }

  companion object {

    private const val KEY_SETTINGS = "is_settings_open"
    private const val KEY_PAGE = "main_page"
  }
}
