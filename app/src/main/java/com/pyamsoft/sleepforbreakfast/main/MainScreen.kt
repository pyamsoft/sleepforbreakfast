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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    appName: String,
    state: MainViewState,
    onOpenSettings: () -> Unit,
    onCloseSettings: () -> Unit,
    onClosePage: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenRepeats: () -> Unit,
) {
  val isSettingsOpen by state.isSettingsOpen.collectAsState()

  Scaffold(
      modifier = modifier.fillMaxSize(),
  ) { pv ->
    Column {
      MainHeader(
          modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.content),
          appName = appName,
          scaffoldPaddingValues = pv,
          onSettingsOpen = onOpenSettings,
      )

      MainContent(
          modifier = Modifier.fillMaxWidth().weight(1F),
          appName = appName,
          state = state,
          onClosePage = onClosePage,
          onOpenTransactions = onOpenTransactions,
          onOpenRepeats = onOpenRepeats,
      )
    }

    if (isSettingsOpen) {
      SettingsDialog(
          modifier = Modifier.fillMaxSize(),
          onDismiss = onCloseSettings,
      )
    }
  }
}
