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

package com.pyamsoft.sleepforbreakfast.repeat.add

import androidx.activity.ComponentActivity
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryColor
import com.pyamsoft.sleepforbreakfast.money.category.CategoryIdMapper
import com.pyamsoft.sleepforbreakfast.ui.SurfaceDialog
import javax.inject.Inject

internal class RepeatAddInjector
@Inject
internal constructor(
    private val params: RepeatAddParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: RepeatAddViewModeler? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusAddRepeat()
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
private fun MountHooks(viewModel: RepeatAddViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) {
    viewModel.bind(
        scope = this,
        force = false,
    )
  }
}

@Composable
internal fun RepeatAddEntry(
    modifier: Modifier = Modifier,
    mapper: CategoryIdMapper,
    params: RepeatAddParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector {
    RepeatAddInjector(
        params = params,
    )
  }

  val viewModel = rememberNotNull(component.viewModel)
  val scope = rememberCoroutineScope()

  MountHooks(
      viewModel = viewModel,
  )

  CompositionLocalProvider(
      LocalCategoryColor provides MaterialTheme.colors.primary,
  ) {
    SurfaceDialog(
        modifier = modifier,
        onDismiss = onDismiss,
    ) {
      RepeatAddScreen(
          state = viewModel,
          mapper = mapper,
          onDismiss = onDismiss,
          onNameChanged = { viewModel.handleNameChanged(it) },
          onNoteChanged = { viewModel.handleNoteChanged(it) },
          onAmountChanged = { viewModel.handleAmountChanged(it) },
          onTypeChanged = { viewModel.handleTypeChanged(it) },
          onCategoryAdded = { viewModel.handleCategoryAdded(it) },
          onCategoryRemoved = { viewModel.handleCategoryRemoved(it) },
          onDateChanged = { viewModel.handleDateChanged(it) },
          onOpenDateDialog = { viewModel.handleOpenDateDialog() },
          onCloseDateDialog = { viewModel.handleCloseDateDialog() },
          onRepeatTypeChanged = { viewModel.handleRepeatTypeChanged(it) },
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
