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

package com.pyamsoft.sleepforbreakfast.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

@Suppress("unused")
val Icons.Filled.EventRepeat: ImageVector
  get() {
    if (_eventRepeat != null) {
      return _eventRepeat!!
    }
    _eventRepeat =
        materialIcon(name = "Filled.EventRepeat") {
          materialPath {
            moveTo(21.0f, 12.0f)
            verticalLineTo(6.0f)
            curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
            horizontalLineToRelative(-1.0f)
            verticalLineTo(2.0f)
            horizontalLineToRelative(-2.0f)
            verticalLineToRelative(2.0f)
            horizontalLineTo(8.0f)
            verticalLineTo(2.0f)
            horizontalLineTo(6.0f)
            verticalLineToRelative(2.0f)
            horizontalLineTo(5.0f)
            curveTo(3.9f, 4.0f, 3.0f, 4.9f, 3.0f, 6.0f)
            verticalLineToRelative(14.0f)
            curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
            horizontalLineToRelative(7.0f)
            verticalLineToRelative(-2.0f)
            horizontalLineTo(5.0f)
            verticalLineTo(10.0f)
            horizontalLineToRelative(14.0f)
            verticalLineToRelative(2.0f)
            horizontalLineTo(21.0f)
            close()
            moveTo(15.64f, 20.0f)
            curveToRelative(0.43f, 1.45f, 1.77f, 2.5f, 3.36f, 2.5f)
            curveToRelative(1.93f, 0.0f, 3.5f, -1.57f, 3.5f, -3.5f)
            reflectiveCurveToRelative(-1.57f, -3.5f, -3.5f, -3.5f)
            curveToRelative(-0.95f, 0.0f, -1.82f, 0.38f, -2.45f, 1.0f)
            lineToRelative(1.45f, 0.0f)
            verticalLineTo(18.0f)
            horizontalLineToRelative(-4.0f)
            verticalLineToRelative(-4.0f)
            horizontalLineToRelative(1.5f)
            lineToRelative(0.0f, 1.43f)
            curveTo(16.4f, 14.55f, 17.64f, 14.0f, 19.0f, 14.0f)
            curveToRelative(2.76f, 0.0f, 5.0f, 2.24f, 5.0f, 5.0f)
            reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
            curveToRelative(-2.42f, 0.0f, -4.44f, -1.72f, -4.9f, -4.0f)
            lineTo(15.64f, 20.0f)
            close()
          }
        }
    return _eventRepeat!!
  }

@Suppress("ObjectPropertyName") private var _eventRepeat: ImageVector? = null
