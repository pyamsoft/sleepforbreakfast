package com.pyamsoft.sleepforbreakfast.money

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@JvmField
val LocalCategoryColor: ProvidableCompositionLocal<Color> = staticCompositionLocalOf {
  Color.Unspecified
}
