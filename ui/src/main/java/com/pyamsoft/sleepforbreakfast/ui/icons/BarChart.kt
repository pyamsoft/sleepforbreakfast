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
