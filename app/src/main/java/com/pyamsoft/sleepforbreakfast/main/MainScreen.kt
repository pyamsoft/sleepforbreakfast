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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.ui.util.fillUpToPortraitSize
import com.pyamsoft.sleepforbreakfast.category.CategoryEntry
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.home.HomeEntry
import com.pyamsoft.sleepforbreakfast.main.settings.SettingsDialog
import com.pyamsoft.sleepforbreakfast.repeat.RepeatEntry
import com.pyamsoft.sleepforbreakfast.transaction.TransactionEntry

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    appName: String,
    state: MainViewState,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onClosePage: () -> Unit,
    onOpenTransactions: (DbCategory) -> Unit,
    onOpenAllTransactions: () -> Unit,
    onOpenRepeats: () -> Unit,
    onOpenCategories: () -> Unit,
) {
  val isSettingsOpen by state.isSettingsOpen.collectAsStateWithLifecycle()
  val page by state.page.collectAsStateWithLifecycle()

  Scaffold(
      modifier = modifier.fillMaxSize(),
  ) { pv ->
    Crossfade(
        label = "Main",
        targetState = page,
    ) { p ->
      if (p == null) {
        HomeEntry(
            modifier = Modifier.fillMaxSize().padding(pv),
            appName = appName,
            onOpenTransactions = onOpenTransactions,
            onOpenAllTransactions = onOpenAllTransactions,
            onOpenRepeats = onOpenRepeats,
            onOpenSettings = onOpenSettings,
            onOpenCategories = onOpenCategories,
        )
      } else {
        when (p) {
          is MainPage.Transactions -> {
            TransactionEntry(
                modifier = Modifier.fillMaxSize().padding(pv),
                page = p,
                onDismiss = onClosePage,
            )
          }
          is MainPage.Repeat -> {
            RepeatEntry(
                modifier = Modifier.fillMaxSize().padding(pv),
                onDismiss = onClosePage,
            )
          }
          is MainPage.Category -> {
            CategoryEntry(
                modifier = Modifier.fillMaxSize().padding(pv),
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
}
