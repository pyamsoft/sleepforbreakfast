/*
 * Copyright 2026 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.category

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.fillUpToPortraitSize
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.category.add.CategoryAddEntry
import com.pyamsoft.sleepforbreakfast.category.delete.CategoryDeleteEntry

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

  val addParams by viewModel.addParams.collectAsStateWithLifecycle()
  val deleteParams by viewModel.deleteParams.collectAsStateWithLifecycle()

  MountHooks(
      viewModel = viewModel,
  )

  BackHandler(
      onBack = onDismiss,
  )

  CategoryScreen(
      modifier = modifier,
      showActionButton = true,
      state = viewModel,
      onBack = onDismiss,
      onSearchToggled = { viewModel.handleToggleSearch() },
      onSearchUpdated = { viewModel.handleSearchUpdated(it) },
      onActionButtonClicked = { viewModel.handleAddNewCategory() },
      onCategoryClicked = { viewModel.handleEditCategory(it) },
      onCategoryLongClicked = { viewModel.handleDeleteCategory(it) },
      onCategoryDeleteFinalized = { viewModel.handleDeleteFinalized() },
      onCategoryRestored = { viewModel.handleRestoreDeleted(scope = scope) },
  )

  addParams?.also { p ->
    CategoryAddEntry(
        modifier = Modifier.fillUpToPortraitSize(),
        params = p,
        onDismiss = { viewModel.handleCloseAddCategory() },
    )
  }

  deleteParams?.also { p ->
    CategoryDeleteEntry(
        modifier = Modifier.fillUpToPortraitSize(),
        params = p,
        onDismiss = { viewModel.handleCloseDeleteCategory() },
    )
  }
}
