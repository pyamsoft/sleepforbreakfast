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
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.ui.list.BasicListScreen
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
    onDismiss: () -> Unit,

    // Action
    showActionButton: Boolean,
    onActionButtonClicked: () -> Unit,

    // Items
    onTransactionClicked: (DbTransaction) -> Unit,
    onTransactionLongClicked: (DbTransaction) -> Unit,
    onTransactionRestored: () -> Unit,
    onTransactionDeleteFinalized: () -> Unit,

    // Search
    onSearchToggled: () -> Unit,
    onSearchUpdated: (String) -> Unit,
) {
  val transactions = state.items.collectAsStateList()
  val list = rememberTransactionsWithHeaders(transactions)
  val undoable by state.recentlyDeleted.collectAsState()

  BasicListScreen(
      modifier = modifier,
      showActionButton = showActionButton,
      recentlyDeletedItem = undoable,
      deletedMessage = { "${it.name} Removed" },
      onSnackbarDismissed = onTransactionDeleteFinalized,
      onSnackbarAction = onTransactionRestored,
      onActionButtonClicked = onActionButtonClicked,
  ) { pv ->
    Column {
      Surface(
          modifier = Modifier.fillMaxWidth(),
          color = MaterialTheme.colors.primary,
          elevation = ZeroElevation,
      ) {
        Spacer(
            modifier = Modifier.fillMaxWidth().statusBarsPadding(),
        )
      }

      TransactionTotal(
          modifier = Modifier.fillMaxWidth().padding(pv),
          state = state,
          onDismiss = onDismiss,
          onSearchToggled = onSearchToggled,
          onSearchUpdated = onSearchUpdated,
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
                            onClick = { onTransactionClicked(transaction) },
                            onLongClick = { onTransactionLongClicked(transaction) },
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
  }
}
