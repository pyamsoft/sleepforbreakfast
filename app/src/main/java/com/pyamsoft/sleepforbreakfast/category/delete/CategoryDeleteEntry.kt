/*
 * Copyright 2025 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.category.delete

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.ui.CardDialog
import javax.inject.Inject

internal class CategoryDeleteInjector
@Inject
internal constructor(
    private val params: CategoryDeleteParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: CategoryDeleteViewModeler? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusDeleteCategory()
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
private fun MountHooks(viewModel: CategoryDeleteViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) {
    viewModel.bind(
        scope = this,
        force = false,
    )
  }
}

@Composable
internal fun CategoryDeleteEntry(
    modifier: Modifier = Modifier,
    params: CategoryDeleteParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector {
    CategoryDeleteInjector(
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

  CardDialog(
      modifier = modifier,
      onDismiss = onDismiss,
  ) {
    CategoryDeleteScreen(
        state = viewModel,
        onDismiss = onDismiss,
        onConfirm = { handleSubmit() },
    )
  }
}
