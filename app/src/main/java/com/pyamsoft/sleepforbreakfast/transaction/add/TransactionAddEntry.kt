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

package com.pyamsoft.sleepforbreakfast.transaction.add

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
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddParams
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddScreen
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddViewModeler
import com.pyamsoft.sleepforbreakfast.ui.SurfaceDialog
import javax.inject.Inject

internal class TransactionAddInjector
@Inject
internal constructor(
    private val params: TransactionAddParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: TransactionAddViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusAddTransactions()
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
private fun MountHooks(viewModel: TransactionAddViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) {
    viewModel.bind(
        scope = this,
        force = false,
    )
  }
}

@Composable
internal fun TransactionAddEntry(
    modifier: Modifier = Modifier,
    params: TransactionAddParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector {
    TransactionAddInjector(
        params = params,
    )
  }

  val viewModel = rememberNotNull(component.viewModel)
  val state = viewModel.state

  val scope = rememberCoroutineScope()

  MountHooks(
      viewModel = viewModel,
  )

  SurfaceDialog(
      modifier = modifier,
      onDismiss = onDismiss,
  ) {
    TransactionAddScreen(
        state = state,
        onDismiss = onDismiss,
        onNameChanged = { viewModel.handleNameChanged(it) },
        onNoteChanged = { viewModel.handleNoteChanged(it) },
        onAmountChanged = { viewModel.handleAmountChanged(it) },
        onTypeChanged = { viewModel.handleTypeChanged(it) },
        onOpenTimeDialog = { viewModel.handleOpenTimeDialog() },
        onCloseTimeDialog = { viewModel.handleCloseTimeDialog() },
        onTimeChanged = { viewModel.handleTimeChanged(it) },
        onOpenDateDialog = { viewModel.handleOpenDateDialog() },
        onCloseDateDialog = { viewModel.handleCloseDateDialog() },
        onDateChanged = { viewModel.handleDateChanged(it) },
        onCategoryAdded = { viewModel.handleCategoryAdded(it) },
        onCategoryRemoved = { viewModel.handleCategoryRemoved(it) },
        onRepeatInfoOpen = { viewModel.handleOpenRepeatInfo() },
        onRepeatInfoClosed = { viewModel.handleCloseRepeatInfo() },
        onAutoInfoOpen = { viewModel.handleOpenAutoInfo() },
        onAutoInfoClosed = { viewModel.handleCloseAutoInfo() },
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
