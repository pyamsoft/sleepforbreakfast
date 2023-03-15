package com.pyamsoft.sleepforbreakfast.main

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import javax.inject.Inject

internal class MainInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: MainViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(viewModel: MainViewModeler) {
  SaveStateDisposableEffect(viewModel)
}

@Composable
internal fun MainEntry(
    modifier: Modifier = Modifier,
    appName: String,
) {
  val component = rememberComposableInjector { MainInjector() }
  val viewModel = rememberNotNull(component.viewModel)

  MountHooks(
      viewModel = viewModel,
  )

  MainScreen(
      modifier = modifier,
      appName = appName,
      state = viewModel.state,
      onSettingsOpen = { viewModel.handleOpenSettings() },
      onSettingsClose = { viewModel.handleCloseSettings() },
      onOpenTransactions = { viewModel.handleOpenTransactions() },
      onCloseTransactions = { viewModel.handleCloseTransactions() },
  )
}
