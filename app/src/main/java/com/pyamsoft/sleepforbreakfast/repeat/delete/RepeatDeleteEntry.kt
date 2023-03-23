package com.pyamsoft.sleepforbreakfast.repeat.delete

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

internal class RepeatDeleteInjector
@Inject
internal constructor(
    private val params: RepeatDeleteParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: RepeatDeleteViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusDeleteRepeats()
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
private fun MountHooks(viewModel: RepeatDeleteViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) {
    viewModel.bind(
        scope = this,
        force = false,
    )
  }
}

@Composable
internal fun RepeatDeleteEntry(
    modifier: Modifier = Modifier,
    params: RepeatDeleteParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector {
    RepeatDeleteInjector(
        params = params,
    )
  }

  val viewModel = rememberNotNull(component.viewModel)
  val scope = rememberCoroutineScope()

  val handleDismiss by rememberUpdatedState(onDismiss)

  val handleSubmit by rememberUpdatedState {
    viewModel.handleDelete(scope = scope) { handleDismiss() }
  }

  MountHooks(
      viewModel = viewModel,
  )

  Dialog(
      properties = rememberDialogProperties(),
      onDismissRequest = { handleDismiss() },
  ) {
    Surface(
        modifier = Modifier.padding(MaterialTheme.keylines.content),
        shape = MaterialTheme.shapes.medium,
        elevation = DialogDefaults.Elevation,
    ) {
      RepeatDeleteScreen(
          modifier = modifier,
          state = viewModel.state,
          onDismiss = { handleDismiss() },
          onConfirm = { handleSubmit() },
      )
    }
  }
}
