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

package com.pyamsoft.sleepforbreakfast.home

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.main.MainPage
import com.pyamsoft.sleepforbreakfast.ui.model.TransactionDateRange
import com.pyamsoft.sleepforbreakfast.ui.rememberCurrentLocale
import java.time.Clock
import java.util.Locale
import javax.inject.Inject

internal class HomeInjector @Inject internal constructor(private val locale: Locale) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: HomeViewModeler? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
      .plusHome()
      .create(locale = locale, activity = activity, lifecycle = activity.lifecycle)
      .inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(viewModel: HomeViewModeler) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
internal fun HomeEntry(
  modifier: Modifier = Modifier,
  clock: Clock,
  appName: String,
  onOpenSettings: () -> Unit,
  onOpenPage: (MainPage) -> Unit,
  onOpenAllTransactions: (TransactionDateRange?) -> Unit,
  onOpenTransactions: (DbCategory, TransactionDateRange?) -> Unit,
) {
  val locale = rememberCurrentLocale()
  val component = rememberComposableInjector { HomeInjector(locale = locale) }
  val viewModel = rememberNotNull(component.viewModel)
  val scope = rememberCoroutineScope()

  MountHooks(viewModel = viewModel)

  HomeScreen(
    modifier = modifier,
    clock = clock,
    state = viewModel,
    appName = appName,
    onOpenSettings = onOpenSettings,
    onOpenAllTransactions = onOpenAllTransactions,
    onOpenTransactions = onOpenTransactions,
    onOpenCategories = { onOpenPage(MainPage.Category) },
    onOpenAutomatics = { onOpenPage(MainPage.Automatic) },
    onToggleExpanded = { viewModel.handleToggleExplanation() },
    onOpenNotificationListenerSettings = { viewModel.handleOpenNotificationSettings(scope = scope) },
  )
}
