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

package com.pyamsoft.sleepforbreakfast.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.ui.haptics.LocalHapticManager

@Composable
fun HomeHeader(
    modifier: Modifier = Modifier,
    appName: String,
    onOpenSettings: () -> Unit,
) {
  val hapticManager = LocalHapticManager.current

  Column(
      modifier = modifier,
  ) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          modifier = Modifier.weight(1F),
          text = appName,
          style = MaterialTheme.typography.headlineMedium,
      )
      IconButton(
          onClick = {
            hapticManager?.actionButtonPress()
            onOpenSettings()
          },
      ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Settings",
        )
      }
    }
  }
}
