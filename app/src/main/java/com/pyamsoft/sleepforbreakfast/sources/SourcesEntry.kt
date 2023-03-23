package com.pyamsoft.sleepforbreakfast.sources

import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.sources.add.SourcesAddEntry
import javax.inject.Inject

internal class SourcesInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: SourcesViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).plusSources().create().inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(
    viewModel: SourcesViewModeler,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
internal fun SourcesEntry(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { SourcesInjector() }
  val viewModel = rememberNotNull(component.viewModel)
  val scope = rememberCoroutineScope()

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

      SourcesScreen(
          modifier = modifier,
          state = state,
          onDismiss = onDismiss,
          onAddNewSources = { viewModel.handleAddNewSources() },
          onDeleteSources = { viewModel.handleDeleteSource(it) },
          onEditSources = { viewModel.handleEditSources(it) },
          onSourcesDeleteFinalized = { viewModel.handleDeleteFinalized() },
          onSourcesRestored = { viewModel.handleRestoreDeleted(scope = scope) },
      )
    } else {
      SourcesAddEntry(
          modifier = modifier,
          params = ap,
          onDismiss = { viewModel.handleCloseAddSource() },
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
