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

package com.pyamsoft.sleepforbreakfast.main

import androidx.compose.runtime.saveable.SaveableStateRegistry
import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.core.cast
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.ui.model.TransactionDateRange
import javax.inject.Inject

class MainViewModeler
@Inject
internal constructor(
    override val state: MutableMainViewState,
) : MainViewState by state, AbstractViewModeler<MainViewState>(state) {

  override fun registerSaveState(
      registry: SaveableStateRegistry
  ): List<SaveableStateRegistry.Entry> =
      mutableListOf<SaveableStateRegistry.Entry>().apply {
        val s = state

        registry.registerProvider(KEY_SETTINGS) { s.isSettingsOpen.value }.also { add(it) }
        registry
            .registerProvider(KEY_PAGE) {
              val p = s.page.value
              if (p == null) {
                return@registerProvider null
              } else {
                return@registerProvider when (p) {
                  is MainPage.Category -> p::class.java.name
                  is MainPage.Automatic -> p::class.java.name
                  is MainPage.Transactions -> p.toBundleable()
                }
              }
            }
            .also { add(it) }
      }

  override fun consumeRestoredState(registry: SaveableStateRegistry) {
    val s = state

    registry.consumeRestored(KEY_SETTINGS)?.cast<Boolean>()?.also { s.isSettingsOpen.value = it }

    registry
        .consumeRestored(KEY_PAGE)
        ?.cast<String>()
        ?.let { p ->
          return@let when (p) {
            MainPage.Category::class.java.name -> MainPage.Category
            // Assume P is a category ID as a String
            else -> MainPage.Transactions.fromBundleable(p)
          }
        }
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

  fun handleOpenTransactions(category: DbCategory, range: TransactionDateRange?) {
    state.page.value =
        MainPage.Transactions(
            categoryId = category.id,
            showAllTransactions = false,
            range = range,
        )
  }

  fun handleOpenAllTransactions(
      range: TransactionDateRange?,
  ) {
    state.page.value =
        MainPage.Transactions(
            categoryId = DbCategory.Id.EMPTY,
            showAllTransactions = true,
            range = range,
        )
  }

  fun handleOpenPage(page: MainPage) {
    state.page.value = page
  }

  companion object {

    private const val KEY_SETTINGS = "is_settings_open"
    private const val KEY_PAGE = "main_page"
  }
}
