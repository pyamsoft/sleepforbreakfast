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

package com.pyamsoft.sleepforbreakfast.ui

import androidx.annotation.CheckResult
import androidx.compose.ui.graphics.Color

val COLOR_EARN = Color.Green
val COLOR_SPEND = Color.Red

private val complementaryColorMap by lazy { mutableMapOf<Color, Color>() }

/** Get the complementary color */
@get:CheckResult
val Color.complement: Color
  get() {
    val self = this
    return complementaryColorMap.getOrPut(self) {
      resolveComplementaryColor(self)
    }
  }

/** https://rgbcolorpicker.com/complementary */
@CheckResult
private fun resolveComplementaryColor(color: Color): Color {
  return Color(
      red = 1F - color.red,
      green = 1F - color.green,
      blue = 1F - color.blue,
      alpha = color.alpha,
      colorSpace = color.colorSpace,
  )
}
