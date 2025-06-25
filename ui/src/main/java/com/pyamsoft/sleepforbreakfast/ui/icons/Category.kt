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
val Icons.Filled.Category: ImageVector
  get() {
    if (_category != null) {
      return _category!!
    }
    _category =
        materialIcon(name = "Filled.Category") {
          materialPath {
            moveTo(12.0f, 2.0f)
            lineToRelative(-5.5f, 9.0f)
            horizontalLineToRelative(11.0f)
            close()
          }
          materialPath {
            moveTo(17.5f, 17.5f)
            moveToRelative(-4.5f, 0.0f)
            arcToRelative(4.5f, 4.5f, 0.0f, true, true, 9.0f, 0.0f)
            arcToRelative(4.5f, 4.5f, 0.0f, true, true, -9.0f, 0.0f)
          }
          materialPath {
            moveTo(3.0f, 13.5f)
            horizontalLineToRelative(8.0f)
            verticalLineToRelative(8.0f)
            horizontalLineTo(3.0f)
            close()
          }
        }
    return _category!!
  }

@Suppress("ObjectPropertyName") private var _category: ImageVector? = null
