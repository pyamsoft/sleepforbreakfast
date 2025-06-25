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

package com.pyamsoft.sleepforbreakfast.category.add

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContainerColor
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContentColor
import com.pyamsoft.sleepforbreakfast.ui.CardDialog
import com.pyamsoft.sleepforbreakfast.ui.complement
import javax.inject.Inject

internal class CategoryAddInjector
@Inject
internal constructor(
    private val params: CategoryAddParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: CategoryAddViewModeler? = null

  override fun onInject(activity: ComponentActivity) {
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
  val categoryColor by viewModel.color.collectAsStateWithLifecycle()
  val scope = rememberCoroutineScope()

  val defaultContainerColor = MaterialTheme.colorScheme.primary
  val containerColor =
      remember(
          categoryColor,
          defaultContainerColor,
      ) {
        if (categoryColor == 0L) defaultContainerColor else Color(categoryColor.toULong())
      }

  val defaultContentColor = MaterialTheme.colorScheme.onPrimary
  val contentColor =
      remember(
          categoryColor,
          defaultContentColor,
      ) {
        if (categoryColor == 0L) defaultContentColor else Color(categoryColor.toULong()).complement
      }

  MountHooks(
      viewModel = viewModel,
  )

  CompositionLocalProvider(
      // Category coloring
      LocalCategoryContainerColor provides containerColor,
      LocalCategoryContentColor provides contentColor,
  ) {
    CardDialog(
        modifier = modifier,
        onDismiss = onDismiss,
    ) {
      CategoryAddScreen(
          state = viewModel,
          onDismiss = onDismiss,
          onNameChanged = { viewModel.handleNameChanged(it) },
          onNoteChanged = { viewModel.handleNoteChanged(it) },
          onColorChanged = { viewModel.handleColorChanged(it.value.toLong()) },
          onOpenColorPicker = { viewModel.handleOpenColorPicker() },
          onCloseColorPicker = { viewModel.handleCloseColorPicker() },
          onReset = { viewModel.handleReset() },
          onSubmit = {
            viewModel.handleSubmit(
                scope = scope,
                onDismissAfterUpdated = onDismiss,
            )
          },
      )
    }
  }
}
