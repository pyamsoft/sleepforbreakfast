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

package com.pyamsoft.sleepforbreakfast.ui.icons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

@Suppress("UnusedReceiverParameter")
val Icons.Filled.BarChart: ImageVector
  get() {
    if (_barChart != null) {
      return _barChart!!
    }
    _barChart =
        materialIcon(name = "Filled.BarChart") {
          materialPath {
            moveTo(4.0f, 9.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(11.0f)
            horizontalLineToRelative(-4.0f)
            close()
          }
          materialPath {
            moveTo(16.0f, 13.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(7.0f)
            horizontalLineToRelative(-4.0f)
            close()
          }
          materialPath {
            moveTo(10.0f, 4.0f)
            horizontalLineToRelative(4.0f)
            verticalLineToRelative(16.0f)
            horizontalLineToRelative(-4.0f)
            close()
          }
        }
    return _barChart!!
  }

@Suppress("ObjectPropertyName") private var _barChart: ImageVector? = null
