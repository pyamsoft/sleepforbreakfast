package com.pyamsoft.sleepforbreakfast.money.list

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface ListViewState<T : Any> : UiViewState {
  val loadingState: StateFlow<LoadingState>
  val items: StateFlow<List<T>>
  val itemError: StateFlow<Throwable?>

  val recentlyDeleted: StateFlow<T?>
}

abstract class MutableListViewState<T : Any> protected constructor() : ListViewState<T> {
  final override val loadingState = MutableStateFlow(LoadingState.NONE)
  final override val items = MutableStateFlow(emptyList<T>())
  final override val itemError = MutableStateFlow<Throwable?>(null)
  final override val recentlyDeleted = MutableStateFlow<T?>(null)
}
