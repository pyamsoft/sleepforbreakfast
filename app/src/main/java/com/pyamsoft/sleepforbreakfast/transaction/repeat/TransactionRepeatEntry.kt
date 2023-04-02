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

package com.pyamsoft.sleepforbreakfast.transaction.repeat

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.app.rememberDialogProperties
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.transactions.repeat.TransactionRepeatInfoParams
import com.pyamsoft.sleepforbreakfast.transactions.repeat.TransactionRepeatInfoScreen
import com.pyamsoft.sleepforbreakfast.transactions.repeat.TransactionRepeatViewModeler
import javax.inject.Inject

internal class TransactionRepeatInjector
@Inject
internal constructor(
    private val params: TransactionRepeatInfoParams,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: TransactionRepeatViewModeler? = null

  override fun onInject(activity: FragmentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusRepeatTransactions()
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
private fun MountHooks(viewModel: TransactionRepeatViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
internal fun TransactionRepeatEntry(
    modifier: Modifier = Modifier,
    params: TransactionRepeatInfoParams,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector {
    TransactionRepeatInjector(
        params = params,
    )
  }

  val viewModel = rememberNotNull(component.viewModel)

  MountHooks(
      viewModel = viewModel,
  )

  Dialog(
      properties = rememberDialogProperties(),
      onDismissRequest = onDismiss,
  ) {
    Surface(
        modifier = modifier.padding(MaterialTheme.keylines.content),
        shape = MaterialTheme.shapes.medium,
        elevation = DialogDefaults.Elevation,
    ) {
      TransactionRepeatInfoScreen(
          state = viewModel.state,
          transactionCreatedDate = params.transactionRepeatDate,
          onDismiss = onDismiss,
      )
    }
  }
}
