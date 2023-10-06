package com.pyamsoft.sleepforbreakfast.category

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
internal fun CategoryColor(
    modifier: Modifier = Modifier,
    color: Color,
) {
  Surface(
      modifier = modifier,
      shape = MaterialTheme.shapes.medium,
      color = color,
  ) {}
}
