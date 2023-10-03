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
