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

package com.pyamsoft.sleepforbreakfast.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.home.category.HomeOpenCategory
import com.pyamsoft.sleepforbreakfast.home.repeats.HomeOpenRepeats
import com.pyamsoft.sleepforbreakfast.home.transactions.HomeOpenTransactions

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    appName: String,
    onOpenSettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenRepeats: () -> Unit,
    onOpenCategory: () -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    HomeHeader(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = MaterialTheme.keylines.content)
                .statusBarsPadding(),
        appName = appName,
        onOpenSettings = onOpenSettings,
    )

    LazyColumn(
        modifier = modifier,
    ) {
      item {
        HomeOptions(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
            state = state,
            onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
        )
      }

      item {
        HomeOpenTransactions(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
            onOpen = onOpenTransactions,
        )
      }

      item {
        HomeOpenRepeats(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
            onOpen = onOpenRepeats,
        )
      }
      item {
        HomeOpenCategory(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
            onOpen = onOpenCategory,
        )
      }
    }
  }
}
