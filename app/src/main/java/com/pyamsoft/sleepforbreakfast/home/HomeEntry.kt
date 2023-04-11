/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    onOpenCategories: () -> Unit,
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
      onOpenCategories = onOpenCategories,
  )
}
