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

package com.pyamsoft.sleepforbreakfast.category.add

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.ui.SurfaceDialog
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

  val handleSubmit by rememberUpdatedState { viewModel.handleSubmit(scope = scope) }

  MountHooks(
      viewModel = viewModel,
  )

  SurfaceDialog(
      modifier = modifier,
      onDismiss = onDismiss,
  ) {
    CategoryAddScreen(
        state = viewModel.state,
        onDismiss = onDismiss,
        onNameChanged = { viewModel.handleNameChanged(it) },
        onNoteChanged = { viewModel.handleNoteChanged(it) },
        onReset = { viewModel.handleReset() },
        onSubmit = { handleSubmit() },
    )
  }
}
