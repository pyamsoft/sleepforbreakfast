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

package com.pyamsoft.sleepforbreakfast.transaction

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.fillUpToPortraitSize
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.main.MainPage
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContainerColor
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContentColor
import com.pyamsoft.sleepforbreakfast.transaction.add.TransactionAddEntry
import com.pyamsoft.sleepforbreakfast.transaction.delete.TransactionDeleteEntry
import com.pyamsoft.sleepforbreakfast.transactions.TransactionScreen
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewModeler
import com.pyamsoft.sleepforbreakfast.ui.complement
import java.time.Clock
import javax.inject.Inject

internal class TransactionInjector
@Inject
internal constructor(
    private val page: MainPage.Transactions,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: TransactionViewModeler? = null
  @JvmField @Inject internal var clock: Clock? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusTransactions()
        .create(
            dateRange = page.range,
            categoryId = page.categoryId,
            showAllTransactions = page.showAllTransactions,
        )
        .inject(this)
  }

  override fun onDispose() {
    viewModel = null
    clock = null
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
    page: MainPage.Transactions,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector {
    TransactionInjector(
        page = page,
    )
  }
  val viewModel = rememberNotNull(component.viewModel)
  val clock = rememberNotNull(component.clock)

  val addParams by viewModel.addParams.collectAsStateWithLifecycle()
  val deleteParams by viewModel.deleteParams.collectAsStateWithLifecycle()

  val scope = rememberCoroutineScope()

  val category by viewModel.category.collectAsStateWithLifecycle()

  val defaultContainerColor = MaterialTheme.colorScheme.primary
  val containerColor =
      remember(
          category,
          defaultContainerColor,
      ) {
        val c = category?.color ?: 0L
        if (c == 0L) defaultContainerColor else Color(c.toULong())
      }

  val defaultContentColor = MaterialTheme.colorScheme.onPrimary
  val contentColor =
      remember(
          category,
          defaultContentColor,
      ) {
        val c = category?.color ?: 0L
        if (c == 0L) defaultContentColor else Color(c.toULong()).complement
      }

  MountHooks(
      viewModel = viewModel,
  )

  BackHandler(
      onBack = onDismiss,
  )

  CompositionLocalProvider(
      // Category coloring
      LocalCategoryContainerColor provides containerColor,
      LocalCategoryContentColor provides contentColor,
  ) {
    TransactionScreen(
        modifier = modifier,
        state = viewModel,
        clock = clock,
        range = page.range,

        // Dismiss
        onDismiss = onDismiss,

        // Action
        onActionButtonClicked = { viewModel.handleAddNewTransaction() },

        // Items
        onTransactionClicked = { viewModel.handleEditTransaction(it) },
        onTransactionLongClicked = { viewModel.handleDeleteTransaction(it) },
        onTransactionRestored = { viewModel.handleRestoreDeleted(scope = scope) },
        onTransactionDeleteFinalized = { viewModel.handleDeleteFinalized() },

        // Search
        onSearchToggled = { viewModel.handleToggleSearch() },
        onSearchUpdated = { viewModel.handleSearchUpdated(it) },

        // Date Range
        onDateRangeToggled = { viewModel.handleToggleDateRange() },
        onDateRangeUpdated = { start, end -> viewModel.handleDateRangeUpdated(start, end) },
    )

    addParams?.also { p ->
      TransactionAddEntry(
          modifier = Modifier.fillUpToPortraitSize(),
          params = p,
          onDismiss = { viewModel.handleCloseAddTransaction() },
      )
    }

    deleteParams?.also { p ->
      TransactionDeleteEntry(
          modifier = Modifier.fillUpToPortraitSize(),
          params = p,
          onDismiss = { viewModel.handleCloseDeleteTransaction() },
      )
    }
  }
}
