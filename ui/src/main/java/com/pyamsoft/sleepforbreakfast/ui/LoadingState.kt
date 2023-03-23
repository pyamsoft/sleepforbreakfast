package com.pyamsoft.sleepforbreakfast.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable

@Stable
@Immutable
enum class LoadingState {
  NONE,
  LOADING,
  DONE
}
