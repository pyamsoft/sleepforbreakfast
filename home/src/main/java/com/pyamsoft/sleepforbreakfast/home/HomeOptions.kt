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
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.pydroid.ui.haptics.LocalHapticManager
import com.pyamsoft.sleepforbreakfast.core.PRIVACY_POLICY_URL
import com.pyamsoft.sleepforbreakfast.ui.appendLink

@Composable
internal fun HomeOptions(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    appName: String,
    onOpenNotificationListenerSettings: () -> Unit,
) {
  val isNotificationListenerEnabled by
      state.isNotificationListenerEnabled.collectAsStateWithLifecycle()

  val shape = MaterialTheme.shapes.medium

  val hapticManager = LocalHapticManager.current

  val themeColor =
      MaterialTheme.colors.primary.copy(
          alpha = if (isNotificationListenerEnabled) ContentAlpha.disabled else ContentAlpha.high,
      )
  val highAlpha = if (isNotificationListenerEnabled) ContentAlpha.medium else ContentAlpha.high
  val mediumAlpha =
      if (isNotificationListenerEnabled) ContentAlpha.disabled else ContentAlpha.medium

  Box(
      modifier = modifier.padding(MaterialTheme.keylines.content),
  ) {
    Surface(
        modifier =
            Modifier.fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = themeColor,
                    shape = shape,
                ),
        shape = shape,
        elevation = DialogDefaults.Elevation,
    ) {
      Column(
          modifier =
              Modifier.clickable(
                      enabled = !isNotificationListenerEnabled,
                  ) {
                    hapticManager?.actionButtonPress()
                    onOpenNotificationListenerSettings()
                  }
                  .padding(MaterialTheme.keylines.content),
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              modifier = Modifier.weight(1F),
              text = "Automatic Tracking",
              style =
                  MaterialTheme.typography.h6.copy(
                      fontWeight = FontWeight.W700,
                      color =
                          MaterialTheme.colors.onSurface.copy(
                              alpha = highAlpha,
                          ),
                  ),
          )

          Switch(
              checked = isNotificationListenerEnabled,
              onCheckedChange = null,
          )
        }

        Text(
            modifier = Modifier.padding(top = MaterialTheme.keylines.content),
            text =
                "$appName could automatically enter transaction information for certain purchases.",
            style =
                MaterialTheme.typography.body1.copy(
                    color =
                        MaterialTheme.colors.onSurface.copy(
                            alpha = highAlpha,
                        ),
                ),
        )

        Text(
            modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
            text =
                "Enabling this feature will give $appName the ability to see ALL of your notifications, but it will only take action on the notifications that it knows are related to transactions.",
            style =
                MaterialTheme.typography.body2.copy(
                    color =
                        MaterialTheme.colors.onSurface.copy(
                            alpha = mediumAlpha,
                        ),
                ),
        )

        val textColor =
            MaterialTheme.colors.onSurface.copy(
                alpha = ContentAlpha.disabled,
            )
        val linkColor = MaterialTheme.colors.primary
        val privacyDisclaimer =
            remember(
                textColor,
                linkColor,
                appName,
            ) {
              buildAnnotatedString {
                withStyle(
                    style =
                        SpanStyle(
                            color = textColor,
                        ),
                ) {
                  append("$appName will never use the Notification Listener permission")
                  append(" for anything with your notification data other than the")
                  append(" single stated purpose.")
                  append(" View our ")
                  appendLink(
                      tag = "FAQ",
                      linkColor = linkColor,
                      text = "Privacy Policy",
                      url = PRIVACY_POLICY_URL,
                  )
                  append(" for more details.")
                }
              }
            }

        Text(
            modifier = Modifier.padding(top = MaterialTheme.keylines.typography),
            text = privacyDisclaimer,
            style = MaterialTheme.typography.caption,
        )
      }
    }
  }
}
