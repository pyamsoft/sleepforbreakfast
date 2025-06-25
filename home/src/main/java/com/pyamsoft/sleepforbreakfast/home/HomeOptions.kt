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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.haptics.LocalHapticManager
import com.pyamsoft.pydroid.ui.uri.rememberUriHandler
import com.pyamsoft.sleepforbreakfast.core.PRIVACY_POLICY_URL

private const val PRIVACY_POLICY_TEXT = "Privacy Policy"

@Composable
internal fun HomeOptions(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    appName: String,
    onToggleExpanded: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
) {
  val isNotificationListenerEnabled by
      state.isNotificationListenerEnabled.collectAsStateWithLifecycle()
  val isExpanded by state.isNotificationExplanationExpanded.collectAsStateWithLifecycle()

  val shape = MaterialTheme.shapes.large

  val hapticManager = LocalHapticManager.current

  val themeColor = MaterialTheme.colorScheme.primary

  val uriHandler = rememberUriHandler()

  val handleLinkClicked by rememberUpdatedState { link: LinkAnnotation ->
    if (link is LinkAnnotation.Url) {
      uriHandler.openUri(link.url)
    }
  }

  Box(modifier = modifier.padding(MaterialTheme.keylines.content)) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(width = 2.dp, color = themeColor, shape = shape),
        shape = shape) {
          Column(
              modifier =
                  Modifier.clickable(enabled = !isNotificationListenerEnabled) {
                        hapticManager?.actionButtonPress()
                        onOpenNotificationListenerSettings()
                      }
                      .padding(MaterialTheme.keylines.content)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(
                      modifier = Modifier.weight(1F),
                      text = "Automatic Tracking",
                      style =
                          MaterialTheme.typography.headlineSmall.copy(
                              fontWeight = FontWeight.W700,
                              color = MaterialTheme.colorScheme.onSurface,
                          ),
                  )

                  Switch(checked = isNotificationListenerEnabled, onCheckedChange = null)
                }

                Text(
                    modifier =
                        Modifier.clickable { onToggleExpanded() }
                            .padding(top = MaterialTheme.keylines.content),
                    text =
                        "$appName could automatically enter transaction information for certain purchases.",
                    style =
                        MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface),
                )

                AnimatedVisibility(visible = isExpanded) {
                  Column(modifier = Modifier.padding(vertical = MaterialTheme.keylines.baseline)) {
                    Text(
                        text =
                            "Enabling this feature will give $appName the ability to see ALL of your notifications, but it will only take action on the notifications that it knows are related to transactions.",
                        style =
                            MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant),
                    )

                    val linkColor = MaterialTheme.colorScheme.primary
                    val privacyPolicyBlurb =
                        remember(linkColor) {
                          val rawText = buildString {
                            append("$appName will never use the Notification Listener permission")
                            append(" for anything with your notification data other than the")
                            append(" single stated purpose.")
                            append(" View our ")
                            append(PRIVACY_POLICY_TEXT)
                            append(" for more details.")
                          }
                          val ppIndex = rawText.indexOf(PRIVACY_POLICY_TEXT)

                          val linkStyle =
                              SpanStyle(
                                  color = linkColor, textDecoration = TextDecoration.Underline)

                          val spanStyles =
                              listOf(
                                  AnnotatedString.Range(
                                      linkStyle,
                                      start = ppIndex,
                                      end = ppIndex + PRIVACY_POLICY_TEXT.length))

                          val visualString = AnnotatedString(rawText, spanStyles = spanStyles)

                          // Can only add annotations to builders
                          return@remember AnnotatedString.Builder(visualString)
                              .apply {
                                // Privacy Policy clickable
                                addLink(
                                    url =
                                        LinkAnnotation.Url(
                                            url = PRIVACY_POLICY_URL,
                                            linkInteractionListener = { handleLinkClicked(it) },
                                        ),
                                    start = ppIndex,
                                    end = ppIndex + PRIVACY_POLICY_TEXT.length,
                                )
                              }
                              .toAnnotatedString()
                        }

                    Text(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(top = MaterialTheme.keylines.typography),
                        text = privacyPolicyBlurb,
                        style = MaterialTheme.typography.bodySmall,
                    )
                  }
                }
              }
        }
  }
}
