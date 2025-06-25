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

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.ui.util.fillUpToPortraitSize
import com.pyamsoft.sleepforbreakfast.automatic.AutomaticEntry
import com.pyamsoft.sleepforbreakfast.category.CategoryEntry
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.home.HomeEntry
import com.pyamsoft.sleepforbreakfast.main.settings.SettingsDialog
import com.pyamsoft.sleepforbreakfast.transaction.TransactionEntry
import com.pyamsoft.sleepforbreakfast.ui.model.TransactionDateRange
import java.time.Clock

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    clock: Clock,
    appName: String,
    state: MainViewState,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onClosePage: () -> Unit,
    onOpenTransactions: (DbCategory, TransactionDateRange?) -> Unit,
    onOpenAllTransactions: (TransactionDateRange?) -> Unit,
    onOpenPage: (MainPage) -> Unit,
) {
  val isSettingsOpen by state.isSettingsOpen.collectAsStateWithLifecycle()
  val page by state.page.collectAsStateWithLifecycle()

  Crossfade(
      label = "Main",
      targetState = page,
  ) { p ->
    if (p == null) {
      HomeEntry(
          modifier = modifier,
          clock = clock,
          appName = appName,
          onOpenTransactions = onOpenTransactions,
          onOpenAllTransactions = onOpenAllTransactions,
          onOpenSettings = onOpenSettings,
          onOpenPage = onOpenPage,
      )
    } else {
      when (p) {
        is MainPage.Transactions -> {
          TransactionEntry(
              modifier = modifier,
              page = p,
              onDismiss = onClosePage,
          )
        }
        is MainPage.Category -> {
          CategoryEntry(
              modifier = modifier,
              onDismiss = onClosePage,
          )
        }
        is MainPage.Automatic -> {
          AutomaticEntry(
              modifier = modifier,
              onDismiss = onClosePage,
          )
        }
      }
    }
  }

  if (isSettingsOpen) {
    SettingsDialog(
        modifier = Modifier.fillUpToPortraitSize(),
        onDismiss = onCloseSettings,
    )
  }
}
