/*
 * Copyright 2024 pyamsoft
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

package com.pyamsoft.sleepforbreakfast

import android.app.Activity
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.annotation.CheckResult
import androidx.annotation.ChecksSdkIntAtLeast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.pyamsoft.pydroid.theme.PYDroidTheme
import com.pyamsoft.pydroid.ui.haptics.LocalHapticManager
import com.pyamsoft.pydroid.ui.haptics.rememberHapticManager
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.uri.LocalExternalUriHandler
import com.pyamsoft.pydroid.ui.uri.rememberExternalUriHandler

@Composable
@ChecksSdkIntAtLeast(Build.VERSION_CODES.S)
private fun rememberCanUseDynamic(isMaterialYou: Boolean): Boolean {
  return remember(isMaterialYou) { Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && isMaterialYou }
}

@Composable
@CheckResult
private fun themeColors(
    activity: Activity,
    isDarkMode: Boolean,
    isMaterialYou: Boolean,
): ColorScheme {
  val canUseDynamic = rememberCanUseDynamic(isMaterialYou)

  return remember(
      activity,
      canUseDynamic,
      isDarkMode,
  ) {
    if (isDarkMode) {

      if (canUseDynamic) {
        dynamicDarkColorScheme(activity)
      } else {
        darkColorScheme(
            // TODO custom color theme
            )
      }
    } else {
      if (canUseDynamic) {
        dynamicLightColorScheme(activity)
      } else {
        lightColorScheme(
            // TODO custom color theme
            )
      }
    }
  }
}

@Composable
fun ComponentActivity.SleepForBreakfastTheme(
    theme: Theming.Mode,
    isMaterialYou: Boolean,
    content: @Composable () -> Unit,
) {
  val self = this

  val isDarkMode = theme.getSystemDarkMode()
  val hapticManager = rememberHapticManager()
  val uriHandler = rememberExternalUriHandler()

  PYDroidTheme(
      colorScheme = themeColors(self, isDarkMode, isMaterialYou),
  ) {
    CompositionLocalProvider(
        // We update the LocalContentColor to match our onBackground. This allows the default
        // content color to be more appropriate to the theme background
        LocalContentColor provides MaterialTheme.colorScheme.onBackground,

        // Provide PYDroid optionals
        LocalHapticManager provides hapticManager,

        // External link handler
        LocalExternalUriHandler provides uriHandler,

        // And the render content
        content = content,
    )
  }
}

@Composable
@CheckResult
fun Theming.Mode.getSystemDarkMode(): Boolean {
  val self = this
  val isDarkMode =
      remember(self) {
        when (self) {
          Theming.Mode.LIGHT -> false
          Theming.Mode.DARK -> true
          Theming.Mode.SYSTEM -> null
        }
      }

  return isDarkMode ?: isSystemInDarkTheme()
}
