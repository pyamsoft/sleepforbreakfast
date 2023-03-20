package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import java.time.Month

@Stable
private sealed class TransactionOrHeader private constructor() {

  @Stable data class Transaction(val transaction: DbTransaction) : TransactionOrHeader()

  @Stable data class Header(val month: Month) : TransactionOrHeader()
}

@Composable
@CheckResult
private fun rememberTransactionsWithHeaders(
    transactions: List<DbTransaction>
): List<TransactionOrHeader> {
  return remember(transactions) {
    if (transactions.isEmpty()) {
      return@remember emptyList()
    }

    val list = mutableListOf<TransactionOrHeader>()

    // Keep track of the last month
    var lastSeenMonth = transactions.first().date.month

    // Start by inserting the month header
    list.add(TransactionOrHeader.Header(lastSeenMonth))

    for (t in transactions) {
      val currentMonth = t.date.month

      // If the month changes, insert our month header
      if (currentMonth != lastSeenMonth) {
        lastSeenMonth = currentMonth
        list.add(TransactionOrHeader.Header(lastSeenMonth))
      }

      list.add(TransactionOrHeader.Transaction(t))
    }

    return@remember list
  }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TransactionScreen(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    onAddNewTransaction: () -> Unit,
    onEditTransaction: (DbTransaction) -> Unit,
    onDeleteTransaction: (DbTransaction) -> Unit,
    onTransactionRestored: () -> Unit,
    onTransactionDeleteFinalized: () -> Unit,
    onDismiss: () -> Unit,
) {
  val scaffoldState = rememberScaffoldState()
  val transactions = state.transactions.collectAsStateList()
  val list = rememberTransactionsWithHeaders(transactions)

  Scaffold(
      modifier = modifier,
      scaffoldState = scaffoldState,
      floatingActionButtonPosition = FabPosition.End,
      floatingActionButton = {
        FloatingActionButton(
            onClick = onAddNewTransaction,
        ) {
          Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "Add New Transaction",
          )
        }
      },
  ) { pv ->
    Column {
      Surface(
          modifier = Modifier.fillMaxWidth(),
          color = MaterialTheme.colors.primary,
      ) {
        Spacer(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        )
      }

      TransactionTotal(
          modifier = Modifier.fillMaxWidth().padding(pv),
          transactions = transactions,
      )

      LazyColumn {
        for (li in list) {
          when (li) {
            is TransactionOrHeader.Header -> {
              val month = li.month

              stickyHeader {
                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(bottom = MaterialTheme.keylines.content)
                            .background(color = MaterialTheme.colors.background)
                            .padding(top = MaterialTheme.keylines.content),
                ) {
                  TransactionHeader(
                      modifier =
                          Modifier.fillMaxWidth()
                              .padding(horizontal = MaterialTheme.keylines.baseline),
                      month = month,
                  )
                }
              }
            }
            is TransactionOrHeader.Transaction -> {
              val transaction = li.transaction

              item {
                TransactionCard(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.keylines.content)
                            .padding(bottom = MaterialTheme.keylines.content),
                    contentModifier =
                        Modifier.combinedClickable(
                            onClick = { onEditTransaction(transaction) },
                            onLongClick = { onDeleteTransaction(transaction) },
                        ),
                    transaction = transaction,
                )
              }
            }
          }
        }

        item {
          Spacer(
              modifier =
                  Modifier.padding(pv)
                      // Space to offset the FAB
                      .height(MaterialTheme.keylines.content * 4),
          )
        }
      }
    }

    TransactionSnackbar(
        scaffoldState = scaffoldState,
        state = state,
        onSnackbarAction = onTransactionRestored,
        onSnackbarDismissed = onTransactionDeleteFinalized,
    )
  }
}

@Composable
private fun TransactionSnackbar(
    scaffoldState: ScaffoldState,
    state: TransactionViewState,
    onSnackbarDismissed: () -> Unit,
    onSnackbarAction: () -> Unit,
) {
  val undoable by state.recentlyDeleteTransaction.collectAsState()

  undoable?.also { u ->
    LaunchedEffect(u) {
      val snackbarResult =
          scaffoldState.snackbarHostState.showSnackbar(
              message = "${u.name} Removed",
              duration = SnackbarDuration.Short,
              actionLabel = "Undo",
          )

      when (snackbarResult) {
        SnackbarResult.Dismissed -> {
          onSnackbarDismissed()
        }
        SnackbarResult.ActionPerformed -> {
          onSnackbarAction()
        }
      }
    }
  }
}
