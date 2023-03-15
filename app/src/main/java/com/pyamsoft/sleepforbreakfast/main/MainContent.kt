package com.pyamsoft.sleepforbreakfast.main

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pyamsoft.sleepforbreakfast.home.HomeEntry
import com.pyamsoft.sleepforbreakfast.transaction.TransactionEntry

@Composable
internal fun MainContent(
    modifier: Modifier = Modifier,
    appName: String,
    state: MainViewState,
    onOpenTransactions: () -> Unit,
    onCloseTransactions: () -> Unit,
) {
  val isTransactionsOpen by state.isTransactionsOpen.collectAsState()
  Crossfade(
      targetState = isTransactionsOpen,
  ) { open ->
    if (open) {
      TransactionEntry(
          modifier = modifier,
          onDismiss = onCloseTransactions,
      )
    } else {
      HomeEntry(
          modifier = modifier,
          onOpenTransactions = onOpenTransactions,
      )
    }
  }
}
