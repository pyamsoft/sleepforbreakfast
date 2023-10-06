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

package com.pyamsoft.sleepforbreakfast.transaction

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.material.MaterialTheme
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
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryColor
import com.pyamsoft.sleepforbreakfast.transaction.add.TransactionAddEntry
import com.pyamsoft.sleepforbreakfast.transaction.delete.TransactionDeleteEntry
import com.pyamsoft.sleepforbreakfast.transactions.TransactionScreen
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewModeler
import java.time.Clock
import javax.inject.Inject

internal class TransactionInjector
@Inject
internal constructor(
    private val categoryId: DbCategory.Id,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: TransactionViewModeler? = null
  @JvmField @Inject internal var clock: Clock? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusTransactions()
        .create(
            categoryId = categoryId,
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
    categoryId: DbCategory.Id,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { TransactionInjector(categoryId) }
  val viewModel = rememberNotNull(component.viewModel)
  val clock = rememberNotNull(component.clock)

  val addParams by viewModel.addParams.collectAsStateWithLifecycle()
  val deleteParams by viewModel.deleteParams.collectAsStateWithLifecycle()

  val scope = rememberCoroutineScope()

  val category by viewModel.category.collectAsStateWithLifecycle()

  val defaultColor = MaterialTheme.colors.primary
  val color =
      remember(
          category,
          defaultColor,
      ) {
        val c = category?.color ?: 0L
        if (c == 0L) defaultColor else Color(c.toULong())
      }

  MountHooks(
      viewModel = viewModel,
  )

  BackHandler(
      onBack = onDismiss,
  )

  CompositionLocalProvider(
      LocalCategoryColor provides color,
  ) {
    TransactionScreen(
        modifier = modifier,
        state = viewModel,
        clock = clock,
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
        onSearchToggleAll = { viewModel.handleToggleSearchAll() },

        // Breakdown
        onBreakdownToggled = { viewModel.handleToggleBreakdown() },
        onBreakdownChange = { viewModel.handleSetBreakdownRange(it) },

        // Chart
        onChartToggled = { viewModel.handleToggleChart() },
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
