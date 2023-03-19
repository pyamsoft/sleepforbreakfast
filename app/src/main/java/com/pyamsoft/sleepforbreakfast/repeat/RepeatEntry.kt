package com.pyamsoft.sleepforbreakfast.repeat

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddEntry
import javax.inject.Inject

internal class RepeatInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: RepeatViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).plusRepeats().create().inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(
    viewModel: RepeatViewModeler,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
internal fun RepeatEntry(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { RepeatInjector() }
  val viewModel = rememberNotNull(component.viewModel)

  val state = viewModel.state
  val addParams by state.addParams.collectAsState()
  //  val deleteParams by state.deleteParams.collectAsState()

  MountHooks(
      viewModel = viewModel,
  )

  Crossfade(
      targetState = addParams,
  ) { ap ->
    if (ap == null) {
      BackHandler(
          onBack = onDismiss,
      )

      RepeatScreen(
          modifier = modifier,
          state = state,
          onDismiss = onDismiss,
          onAddNewRepeat = { viewModel.handleAddNewRepeat() },
      )
    } else {
      RepeatAddEntry(
          modifier = modifier,
          params = ap,
          onDismiss = { viewModel.handleCloseAddRepeat() },
      )
    }
  }
  //
  //  deleteParams?.also { p ->
  //    TransactionDeleteEntry(
  //        params = p,
  //        onDismiss = { viewModel.handleCloseDeleteTransaction() },
  //    )
  //  }
}
