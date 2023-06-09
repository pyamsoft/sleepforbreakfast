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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.sleepforbreakfast.getSystemDarkMode

@Composable
internal fun SystemBars(
    theme: Theming.Mode,
) {
  // Dark icons in Light mode only
  val darkMode = theme.getSystemDarkMode()
  val darkIcons = remember(darkMode) { !darkMode }

  val controller = rememberSystemUiController()
  SideEffect {
    controller.setSystemBarsColor(
        color = Color.Transparent,
        darkIcons = darkIcons,
        isNavigationBarContrastEnforced = false,
    )
  }
}
