package com.pyamsoft.sleepforbreakfast.category.add

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

internal class CategoryAddInjector
@Inject
internal constructor(
    private val params: CategoryAddParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: CategoryAddViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusAddCategory()
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
private fun MountHooks(viewModel: CategoryAddViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) {
    viewModel.bind(
        scope = this,
        force = false,
    )
  }
}

@Composable
internal fun CategoryAddEntry(
    modifier: Modifier = Modifier,
    params: CategoryAddParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector {
    CategoryAddInjector(
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
      CategoryAddScreen(
          state = viewModel.state,
          onDismiss = { handleDismiss() },
          onNameChanged = { viewModel.handleNameChanged(it) },
          onNoteChanged = { viewModel.handleNoteChanged(it) },
          onReset = { viewModel.handleReset() },
          onSubmit = { handleSubmit() },
      )
    }
  }
}
