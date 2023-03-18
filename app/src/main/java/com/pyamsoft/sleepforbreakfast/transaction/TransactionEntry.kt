package com.pyamsoft.sleepforbreakfast.transaction

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

  Crossfade(
      targetState = addParams,
  ) { ap ->
    if (ap == null) {
      BackHandler(
          onBack = onDismiss,
      )

      TransactionScreen(
          modifier = modifier,
          state = state,
          onDismiss = onDismiss,
          onAddNewTransaction = { viewModel.handleAddNewTransaction() },
          onEditTransaction = { viewModel.handleEditTransaction(it) },
          onDeleteTransaction = { viewModel.handleDeleteTransaction(it) },
          onTransactionRestored = { viewModel.handleRestoreDeletedTransaction(scope = scope) },
          onTransactionDeleteFinalized = { viewModel.handleTransactionDeleteFinal() },
      )
    } else {
      TransactionAddEntry(
          modifier = modifier,
          params = ap,
          onDismiss = { viewModel.handleCloseAddTransaction() },
      )
    }
  }

  deleteParams?.also { p ->
    TransactionDeleteEntry(
        params = p,
        onDismiss = { viewModel.handleCloseDeleteTransaction() },
    )
  }
}
