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

package com.pyamsoft.sleepforbreakfast.ui

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

@Composable
fun Delayed(
    modifier: Modifier = Modifier,
    wait: Duration = 250.milliseconds,
    label: String = "Loading",
    content: @Composable () -> Unit,
) {
  val (show, setShow) = remember { mutableStateOf(false) }

  LaunchedEffect(
      wait,
  ) {
    delay(wait)
    setShow(true)
  }

  Crossfade(
      modifier = modifier,
      label = label,
      targetState = show,
  ) { s ->
    if (s) {
      content()
    }
  }
}
