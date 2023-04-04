package com.pyamsoft.sleepforbreakfast.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.delay

/** Do this so that we can debounce typing events */
@Composable
fun debouncedOnTextChange(
    value: String,
    onChange: (String) -> Unit,
): MutableState<String> {
  return debouncedOnTextChange(
      value = value,
      delay = 300L,
      onChange = onChange,
  )
}

/** Do this so that we can debounce typing events */
@Composable
fun debouncedOnTextChange(
    value: String,
    delay: Long,
    onChange: (String) -> Unit,
): MutableState<String> {
  val handleChange by rememberUpdatedState(onChange)

  val ret = remember { mutableStateOf(value) }
  val current = ret.value

  // Each time the current changes, delay by a set amount and then fire the change out
  // effectively deboucning changes while keeping track of the text
  LaunchedEffect(
      current,
      delay,
  ) {
    delay(delay)
    handleChange(current)
  }

  // When this Composable leaves the render pipe, we fire off a final change event
  // To make sure that outside callers know about the most recent value
  val mostRecentValue by rememberUpdatedState(current)
  DisposableEffect(Unit) {
    onDispose {
      // Both of these values are remembered
      // This way the disposable only mounts once, and then
      // upon it finally leaving, we fire the most up-to-date value out
      handleChange(mostRecentValue)
    }
  }

  return ret
}
