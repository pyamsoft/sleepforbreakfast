/*
 * Copyright 2024 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.main

import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.theme.Theming
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryColor
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryObserver
import com.pyamsoft.sleepforbreakfast.money.LocalTransactionObserver
import com.pyamsoft.sleepforbreakfast.money.observer.CategoryObserver
import com.pyamsoft.sleepforbreakfast.money.observer.TransactionObserver
import java.time.Clock
import javax.inject.Inject

internal class MainInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var transactionObserver: TransactionObserver? = null
  @JvmField @Inject internal var categoryObserver: CategoryObserver? = null
  @JvmField @Inject internal var viewModel: MainViewModeler? = null
  @JvmField @Inject internal var clock: Clock? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).inject(this)
  }

  override fun onDispose() {
    viewModel = null
    categoryObserver = null
    transactionObserver = null
    clock = null
  }
}

@Composable
private fun MountHooks(
    viewModel: MainViewModeler,
    categoryObserver: CategoryObserver,
    transactionObserver: TransactionObserver,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(categoryObserver) { categoryObserver.bind(scope = this) }
  LaunchedEffect(transactionObserver) { transactionObserver.bind(scope = this) }
}

@Composable
internal fun ComponentActivity.MainEntry(
    modifier: Modifier = Modifier,
    appName: String,
    theme: Theming.Mode,
) {
  val component = rememberComposableInjector { MainInjector() }
  val viewModel = rememberNotNull(component.viewModel)
  val categoryObserver = rememberNotNull(component.categoryObserver)
  val transactionObserver = rememberNotNull(component.transactionObserver)
  val clock = rememberNotNull(component.clock)

  val page by viewModel.page.collectAsStateWithLifecycle()
  val isDarkIcons = remember(page) { page == null }
  SystemBars(
      theme = theme,
      isDarkIcons = isDarkIcons,
  )

  MountHooks(
      viewModel = viewModel,
      categoryObserver = categoryObserver,
      transactionObserver = transactionObserver,
  )

  CompositionLocalProvider(
      LocalCategoryColor provides MaterialTheme.colorScheme.primary,
      LocalCategoryObserver provides categoryObserver,
      LocalTransactionObserver provides transactionObserver,
  ) {
    MainScreen(
        modifier = modifier,
        clock = clock,
        appName = appName,
        state = viewModel,
        onOpenSettings = { viewModel.handleOpenSettings() },
        onCloseSettings = { viewModel.handleCloseSettings() },
        onClosePage = { viewModel.handleClosePage() },
        onOpenTransactions = { category, range ->
          viewModel.handleOpenTransactions(category, range)
        },
        onOpenAllTransactions = { viewModel.handleOpenAllTransactions(it) },
        onOpenPage = { viewModel.handleOpenPage(it) },
    )
  }
}
