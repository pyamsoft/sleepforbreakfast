package com.pyamsoft.sleepforbreakfast.repeat.add

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.app.rememberDialogProperties
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import javax.inject.Inject

internal class RepeatAddInjector
@Inject
internal constructor(
    private val params: RepeatAddParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: RepeatAddViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusAddRepeat()
        .create(
            params = params,
        )
        .inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(viewModel: RepeatAddViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
internal fun RepeatAddEntry(
    modifier: Modifier = Modifier,
    params: RepeatAddParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector {
    RepeatAddInjector(
        params = params,
    )
  }

  val viewModel = rememberNotNull(component.viewModel)
  val scope = rememberCoroutineScope()

  val handleDismiss by rememberUpdatedState(onDismiss)

  val handleSubmit by rememberUpdatedState {
    viewModel.handleSubmit(scope = scope) { handleDismiss() }
  }

  MountHooks(
      viewModel = viewModel,
  )

  Dialog(
      properties = rememberDialogProperties(),
      onDismissRequest = { handleDismiss() },
  ) {
    Surface(
        modifier = modifier.padding(MaterialTheme.keylines.content),
        elevation = DialogDefaults.Elevation,
        shape = MaterialTheme.shapes.medium,
    ) {
      RepeatAddScreen(
          state = viewModel.state,
          onDismiss = { handleDismiss() },
          onNameChanged = { viewModel.handleNameChanged(it) },
          onNoteChanged = { viewModel.handleNoteChanged(it) },
          onAmountChanged = { viewModel.handleAmountChanged(it) },
          onTypeChanged = { viewModel.handleTypeChanged(it) },
          onReset = { viewModel.handleReset() },
          onSubmit = { handleSubmit() },
      )
    }
  }
}
