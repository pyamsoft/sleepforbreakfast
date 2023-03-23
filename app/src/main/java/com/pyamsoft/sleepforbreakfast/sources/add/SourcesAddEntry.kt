package com.pyamsoft.sleepforbreakfast.sources.add

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

internal class SourcesAddInjector
@Inject
internal constructor(
    private val params: SourcesAddParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: SourcesAddViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusAddSources()
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
private fun MountHooks(viewModel: SourcesAddViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) {
    viewModel.bind(
        scope = this,
        force = false,
    )
  }
}

@Composable
internal fun SourcesAddEntry(
    modifier: Modifier = Modifier,
    params: SourcesAddParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector {
    SourcesAddInjector(
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
      SourcesAddScreen(
          state = viewModel.state,
          onDismiss = { handleDismiss() },
          onNameChanged = { viewModel.handleNameChanged(it) },
          onNoteChanged = { viewModel.handleNoteChanged(it) },
          onAccountNumberChanged = { viewModel.handleAccountNumberChanged(it) },
          onReset = { viewModel.handleReset() },
          onSubmit = { handleSubmit() },
      )
    }
  }
}
