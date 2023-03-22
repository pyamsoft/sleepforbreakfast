package com.pyamsoft.sleepforbreakfast.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import javax.inject.Inject

internal class HomeInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: HomeViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusHome()
        .create(
            activity = activity,
        )
        .inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(viewModel: HomeViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
internal fun HomeEntry(
    modifier: Modifier = Modifier,
    appName: String,
    onOpenSettings: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenRepeats: () -> Unit,
    onOpenSources: () -> Unit,
) {
  val component = rememberComposableInjector { HomeInjector() }
  val viewModel = rememberNotNull(component.viewModel)
  val scope = rememberCoroutineScope()

  MountHooks(
      viewModel = viewModel,
  )

  HomeScreen(
      modifier = modifier,
      state = viewModel.state,
      appName = appName,
      onOpenSettings = onOpenSettings,
      onOpenNotificationListenerSettings = {
        viewModel.handleOpenNotificationSettings(scope = scope)
      },
      onOpenTransactions = onOpenTransactions,
      onOpenRepeats = onOpenRepeats,
      onOpenSources = onOpenSources,
  )
}
