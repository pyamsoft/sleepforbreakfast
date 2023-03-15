package com.pyamsoft.sleepforbreakfast.ui

import android.content.res.Configuration
import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.util.Locale

/** Gets the current locale */
@Composable
fun rememberCurrentLocale(): Locale {
  val configuration = LocalConfiguration.current
  return remember(configuration) { newGetCurrentLocale(configuration) }
}

@CheckResult
private fun newGetCurrentLocale(configuration: Configuration): Locale {
  return configuration.locales[0]
}
