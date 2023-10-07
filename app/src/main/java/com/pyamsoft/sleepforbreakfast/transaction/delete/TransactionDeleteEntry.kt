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

package com.pyamsoft.sleepforbreakfast.transaction.delete

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.category.CategoryIdMapper
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteParams
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteScreen
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteViewModeler
import com.pyamsoft.sleepforbreakfast.ui.SurfaceDialog
import javax.inject.Inject

internal class TransactionDeleteInjector
@Inject
internal constructor(
    private val params: TransactionDeleteParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: TransactionDeleteViewModeler? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusDeleteTransactions()
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
private fun MountHooks(viewModel: TransactionDeleteViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) {
    viewModel.bind(
        scope = this,
        force = false,
    )
  }
}

@Composable
internal fun TransactionDeleteEntry(
    modifier: Modifier = Modifier,
    params: TransactionDeleteParams,
    mapper: CategoryIdMapper,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector {
    TransactionDeleteInjector(
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

  SurfaceDialog(
      modifier = modifier,
      onDismiss = onDismiss,
  ) {
    TransactionDeleteScreen(
        state = viewModel,
        mapper = mapper,
        onDismiss = onDismiss,
        onConfirm = { handleSubmit() },
    )
  }
}
