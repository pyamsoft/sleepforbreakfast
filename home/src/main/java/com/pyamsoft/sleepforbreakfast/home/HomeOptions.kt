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

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.pydroid.ui.haptics.LocalHapticManager

@Composable
internal fun HomeOptions(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    onOpenNotificationListenerSettings: () -> Unit,
) {
  val isNotificationListenerEnabled by state.isNotificationListenerEnabled.collectAsStateWithLifecycle()

  val shape = MaterialTheme.shapes.medium

  val hapticManager = LocalHapticManager.current

  Box(
      modifier = modifier.padding(MaterialTheme.keylines.content),
  ) {
    Surface(
        modifier =
            Modifier.fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colors.primary,
                    shape = shape,
                ),
        shape = shape,
        elevation = DialogDefaults.Elevation,
    ) {
      Column(
          modifier =
              Modifier.clickable {
                    hapticManager?.actionButtonPress()
                    onOpenNotificationListenerSettings()
                  }
                  .padding(MaterialTheme.keylines.content),
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = "Watch Notifications for spending",
              style =
                  MaterialTheme.typography.body1.copy(
                      fontWeight = FontWeight.W700,
                  ),
          )

          Switch(
              checked = isNotificationListenerEnabled,
              onCheckedChange = null,
          )
        }
      }
    }
  }
}
