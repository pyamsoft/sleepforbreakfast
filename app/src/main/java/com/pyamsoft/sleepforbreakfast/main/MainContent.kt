package com.pyamsoft.sleepforbreakfast.main

import androidx.compose.animation.Crossfade
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pyamsoft.sleepforbreakfast.home.HomeEntry
import com.pyamsoft.sleepforbreakfast.repeat.RepeatEntry
import com.pyamsoft.sleepforbreakfast.transaction.TransactionEntry

@Composable
internal fun MainContent(
    modifier: Modifier = Modifier,
    appName: String,
    state: MainViewState,
    onClosePage: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenRepeats: () -> Unit,
) {
  val page by state.page.collectAsState()

  Crossfade(
      targetState = page,
  ) { p ->
    if (p == null) {
      HomeEntry(
          modifier = modifier,
          onOpenTransactions = onOpenTransactions,
          onOpenRepeats = onOpenRepeats,
      )
    } else {
      when (p) {
        MainPage.TRANSACTION -> {
          TransactionEntry(
              modifier = modifier,
              onDismiss = onClosePage,
          )
        }
        MainPage.REPEAT -> {
          RepeatEntry(
              modifier = modifier,
              onDismiss = onClosePage,
          )
        }
      }
    }
  }
}
