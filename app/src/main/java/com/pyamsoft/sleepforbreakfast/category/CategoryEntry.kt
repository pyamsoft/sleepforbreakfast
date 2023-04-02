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

package com.pyamsoft.sleepforbreakfast.category

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
import com.pyamsoft.pydroid.ui.util.fullScreenDialog
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.category.add.CategoryAddEntry
import com.pyamsoft.sleepforbreakfast.category.delete.CategoryDeleteEntry
import javax.inject.Inject

internal class CategoryInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: CategoryViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).plusCategory().create().inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(
    viewModel: CategoryViewModeler,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
internal fun CategoryEntry(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { CategoryInjector() }
  val viewModel = rememberNotNull(component.viewModel)
  val scope = rememberCoroutineScope()

  val state = viewModel.state
  val addParams by state.addParams.collectAsState()
  val deleteParams by state.deleteParams.collectAsState()

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

      CategoryScreen(
          modifier = modifier,
          showActionButton = true,
          state = state,
          onActionButtonClicked = { viewModel.handleAddNewCategory() },
          onCategoryClicked = { viewModel.handleEditCategory(it) },
          onCategoryLongClicked = { viewModel.handleDeleteCategory(it) },
          onCategoryDeleteFinalized = { viewModel.handleDeleteFinalized() },
          onCategoryRestored = { viewModel.handleRestoreDeleted(scope = scope) },
      )
    } else {
      CategoryAddEntry(
          modifier = Modifier.fullScreenDialog(),
          params = ap,
          onDismiss = { viewModel.handleCloseAddCategory() },
      )
    }
  }

  deleteParams?.also { p ->
    CategoryDeleteEntry(
        modifier = Modifier.fullScreenDialog(),
        params = p,
        onDismiss = { viewModel.handleCloseDeleteCategory() },
    )
  }
}
