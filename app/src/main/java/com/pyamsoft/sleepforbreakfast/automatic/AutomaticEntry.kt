/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.automatic

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.fillUpToPortraitSize
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.automatic.add.AutomaticAddEntry
import com.pyamsoft.sleepforbreakfast.automatic.delete.AutomaticDeleteEntry
import javax.inject.Inject

internal class AutomaticInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: AutomaticViewModeler? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).plusAutomatic().create().inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(
    viewModel: AutomaticViewModeler,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
internal fun AutomaticEntry(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { AutomaticInjector() }
  val viewModel = rememberNotNull(component.viewModel)

  val addParams by viewModel.addParams.collectAsStateWithLifecycle()
  val deleteParams by viewModel.deleteParams.collectAsStateWithLifecycle()

  // Use the LifecycleOwner.CoroutineScope (Activity usually)
  // so that the scope does not die because of navigation events
  val owner = LocalLifecycleOwner.current
  val lifecycleScope = owner.lifecycleScope

  MountHooks(
      viewModel = viewModel,
  )

  BackHandler(
      onBack = onDismiss,
  )

  AutomaticScreen(
      modifier = modifier,
      showActionButton = true,
      state = viewModel,
      onBack = onDismiss,
      onSearchToggled = { viewModel.handleToggleSearch() },
      onSearchUpdated = { viewModel.handleSearchUpdated(it) },
      onActionButtonClicked = { viewModel.handleAddNewAutomatic() },
      onAutomaticClicked = { viewModel.handleEditAutomatic(it) },
      onAutomaticLongClicked = { viewModel.handleDeleteAutomatic(it) },
      onAutomaticDeleteFinalized = { viewModel.handleDeleteFinalized() },
      onAutomaticRestored = { viewModel.handleRestoreDeleted(scope = lifecycleScope) },
  )

  addParams?.also { p ->
    AutomaticAddEntry(
        modifier = Modifier.fillUpToPortraitSize(),
        params = p,
        onDismiss = { viewModel.handleCloseAddAutomatic() },
    )
  }

  deleteParams?.also { p ->
    AutomaticDeleteEntry(
        modifier = Modifier.fillUpToPortraitSize(),
        params = p,
        onDismiss = { viewModel.handleCloseDeleteAutomatic() },
    )
  }
}
