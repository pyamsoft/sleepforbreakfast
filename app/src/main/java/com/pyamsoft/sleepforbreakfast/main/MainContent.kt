package com.pyamsoft.sleepforbreakfast.main

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.pyamsoft.sleepforbreakfast.home.HomeEntry
import com.pyamsoft.sleepforbreakfast.repeat.RepeatEntry
import com.pyamsoft.sleepforbreakfast.transaction.TransactionEntry

@Composable
internal fun MainContent(
    modifier: Modifier = Modifier,
    appName: String,
    state: MainViewState,
    onOpenTransactions: () -> Unit,
    onCloseTransactions: () -> Unit,
    onOpenRepeats: () -> Unit,
    onCloseRepeats: () -> Unit,
) {
  val isTransactionsOpen by state.isTransactionsOpen.collectAsState()
  val isRepeatOpen by state.isRepeatOpen.collectAsState()

  val opens =
      remember(
          isTransactionsOpen,
          isRepeatOpen,
      ) {
        if (!isTransactionsOpen && !isRepeatOpen) {
          null
        } else {
          OpenScreens(
              transactions = isTransactionsOpen,
              repeats = isRepeatOpen,
          )
        }
      }

  Crossfade(
      targetState = opens,
  ) { open ->
    if (open == null) {
      HomeEntry(
          modifier = modifier,
          onOpenTransactions = onOpenTransactions,
          onOpenRepeats = onOpenRepeats,
      )
    } else {
      if (open.transactions) {
        TransactionEntry(
            modifier = modifier,
            onDismiss = onCloseTransactions,
        )
      } else if (open.repeats) {
        RepeatEntry(
            modifier = modifier,
            onDismiss = onCloseRepeats,
        )
      }
    }
  }
}

private data class OpenScreens(
    val transactions: Boolean,
    val repeats: Boolean,
)
