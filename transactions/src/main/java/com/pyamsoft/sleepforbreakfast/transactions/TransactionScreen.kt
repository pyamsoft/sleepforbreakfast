/*
 * Copyright 2025 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContainerColor
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContentColor
import com.pyamsoft.sleepforbreakfast.transactions.list.TransactionCard
import com.pyamsoft.sleepforbreakfast.transactions.list.TransactionHeader
import com.pyamsoft.sleepforbreakfast.transactions.list.TransactionOrHeader
import com.pyamsoft.sleepforbreakfast.transactions.list.TransactionTotal
import com.pyamsoft.sleepforbreakfast.transactions.list.rememberTransactionsWithHeaders
import com.pyamsoft.sleepforbreakfast.ui.list.BasicListScreen
import com.pyamsoft.sleepforbreakfast.ui.model.TransactionDateRange
import com.pyamsoft.sleepforbreakfast.ui.renderPYDroidExtras
import java.time.Clock
import java.time.LocalDate

private enum class ContentTypes {
  HEADER,
  TRANSACTION,
  BOTTOM_SPACER,
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun TransactionScreen(
    modifier: Modifier = Modifier,
    clock: Clock,
    state: TransactionViewState,
    range: TransactionDateRange?,
    onDismiss: () -> Unit,

    // Action
    onActionButtonClicked: () -> Unit,

    // Items
    onTransactionClicked: (DbTransaction) -> Unit,
    onTransactionLongClicked: (DbTransaction) -> Unit,
    onTransactionRestored: () -> Unit,
    onTransactionDeleteFinalized: () -> Unit,

    // Search
    onSearchToggled: () -> Unit,
    onSearchUpdated: (String) -> Unit,

    // Date Range
    onDateRangeToggled: () -> Unit,
    onDateRangeUpdated: (LocalDate, LocalDate) -> Unit,
) {
  val showActionButton by state.showActionButton.collectAsStateWithLifecycle()
  val currentCategory by state.category.collectAsStateWithLifecycle()
  val loading by state.loadingState.collectAsStateWithLifecycle()
  val transactions = state.items.collectAsStateListWithLifecycle()
  val list = rememberTransactionsWithHeaders(transactions)
  val undoable by state.recentlyDeleted.collectAsStateWithLifecycle()

  BasicListScreen(
      modifier = modifier,
      loading = loading,
      actionButtonContainerColor = LocalCategoryContainerColor.current,
      actionButtonContentColor = LocalCategoryContentColor.current,
      showActionButton = showActionButton,
      recentlyDeletedItem = undoable,
      deletedMessage = { "${it.name} Removed" },
      onSnackbarDismissed = onTransactionDeleteFinalized,
      onSnackbarAction = onTransactionRestored,
      onActionButtonClicked = onActionButtonClicked,
  ) { pv ->
    Column {
      TransactionTotal(
          modifier =
              Modifier.fillMaxWidth()
                  // So this basically doesn't do anything since we handle the padding ourselves
                  // BUT, we don't just want to consume it because we DO actually care when using
                  // Modifier.navigationBarsPadding()
                  .heightIn(
                      min = remember(pv) { pv.calculateBottomPadding() },
                  ),
          clock = clock,
          state = state,
          range = range,
          onDismiss = onDismiss,
          onSearchToggle = onSearchToggled,
          onSearchChange = onSearchUpdated,
          onDateRangeToggle = onDateRangeToggled,
          onDateRangeChange = onDateRangeUpdated,
      )

      LazyColumn {
        renderPYDroidExtras()

        for (li in list) {
          when (li) {
            is TransactionOrHeader.Header -> {
              val month = li.month

              stickyHeader(
                  contentType = ContentTypes.HEADER,
              ) {
                Column(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(bottom = MaterialTheme.keylines.content)
                            .background(color = MaterialTheme.colorScheme.background)
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
              currentCategory?.also { cur ->
                item(
                    contentType = ContentTypes.TRANSACTION,
                ) {
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
                      currentCategory = cur.id,
                  )
                }
              }
            }
          }
        }

        item(
            contentType = ContentTypes.BOTTOM_SPACER,
        ) {
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
