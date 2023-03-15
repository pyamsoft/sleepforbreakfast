package com.pyamsoft.sleepforbreakfast.transaction.add

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.app.rememberDialogProperties
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddParams
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddScreen
import com.pyamsoft.sleepforbreakfast.transactions.add.TransactionAddViewModeler
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

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
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
  val scope = rememberCoroutineScope()

  val handleDismiss by rememberUpdatedState(onDismiss)

  val handleSubmit by rememberUpdatedState {
    viewModel.handleSubmit(scope = scope) { handleDismiss() }
  }

  MountHooks(
      viewModel = viewModel,
  )

  Dialog(
      properties = rememberDialogProperties(),
      onDismissRequest = { handleDismiss() },
  ) {
    TransactionAddScreen(
        modifier = modifier.padding(MaterialTheme.keylines.content),
        state = viewModel.state,
        onDismiss = { handleDismiss() },
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
        onReset = { viewModel.handleReset() },
        onSubmit = { handleSubmit() },
    )
  }
}
