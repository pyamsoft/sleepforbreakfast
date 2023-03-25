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
