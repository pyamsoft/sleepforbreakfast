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

package com.pyamsoft.sleepforbreakfast.transaction

import androidx.activity.compose.BackHandler
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
import com.pyamsoft.sleepforbreakfast.transaction.add.TransactionAddEntry
import com.pyamsoft.sleepforbreakfast.transaction.delete.TransactionDeleteEntry
import com.pyamsoft.sleepforbreakfast.transactions.TransactionScreen
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewModeler
import javax.inject.Inject

internal class TransactionInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: TransactionViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).plusTransactions().create().inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(
    viewModel: TransactionViewModeler,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
internal fun TransactionEntry(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { TransactionInjector() }
  val viewModel = rememberNotNull(component.viewModel)
  val scope = rememberCoroutineScope()

  val state = viewModel.state
  val addParams by state.addParams.collectAsState()
  val deleteParams by state.deleteParams.collectAsState()

  MountHooks(
      viewModel = viewModel,
  )

  BackHandler(
      onBack = onDismiss,
  )

  TransactionScreen(
      modifier = modifier,
      state = state,
      onDismiss = onDismiss,
      // Action
      showActionButton = true,
      onActionButtonClicked = { viewModel.handleAddNewTransaction() },

      // Items
      onTransactionClicked = { viewModel.handleEditTransaction(it) },
      onTransactionLongClicked = { viewModel.handleDeleteTransaction(it) },
      onTransactionRestored = { viewModel.handleRestoreDeleted(scope = scope) },
      onTransactionDeleteFinalized = { viewModel.handleDeleteFinalized() },

      // Search
      onSearchToggled = { viewModel.handleToggleSearch() },
      onSearchUpdated = { viewModel.handleSearchUpdated(it) },

      // Breakdown
      onBreakdownToggled = { viewModel.handleToggleBreakdown() },
      onBreakdownChange = { viewModel.handleSetBreakdownRange(it) },
  )

  addParams?.also { p ->
    TransactionAddEntry(
        modifier = Modifier.fullScreenDialog(),
        params = p,
        onDismiss = { viewModel.handleCloseAddTransaction() },
    )
  }

  deleteParams?.also { p ->
    TransactionDeleteEntry(
        modifier = Modifier.fullScreenDialog(),
        params = p,
        onDismiss = { viewModel.handleCloseDeleteTransaction() },
    )
  }
}
