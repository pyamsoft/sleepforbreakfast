package com.pyamsoft.sleepforbreakfast.ui

import androidx.compose.runtime.Composable
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

  LaunchedEffect(
      current,
      delay,
  ) {
    delay(delay)
    handleChange(current)
  }

  return ret
}
