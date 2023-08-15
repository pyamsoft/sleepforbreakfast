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

package com.pyamsoft.sleepforbreakfast.repeat

import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.arch.SaveStateDisposableEffect
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.pydroid.ui.inject.rememberComposableInjector
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.pydroid.ui.util.fillUpToPortraitSize
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.money.list.Search
import com.pyamsoft.sleepforbreakfast.money.list.SearchBar
import com.pyamsoft.sleepforbreakfast.repeat.add.RepeatAddEntry
import com.pyamsoft.sleepforbreakfast.repeat.delete.RepeatDeleteEntry
import javax.inject.Inject

internal class RepeatInjector @Inject internal constructor() : ComposableInjector() {

  @JvmField @Inject internal var viewModel: RepeatViewModeler? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity).plusRepeats().create().inject(this)
  }

  override fun onDispose() {
    viewModel = null
  }
}

@Composable
private fun MountHooks(
    viewModel: RepeatViewModeler,
) {
  SaveStateDisposableEffect(viewModel)

  LaunchedEffect(viewModel) { viewModel.bind(scope = this) }
}

@Composable
internal fun RepeatEntry(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
  val component = rememberComposableInjector { RepeatInjector() }
  val viewModel = rememberNotNull(component.viewModel)
  val scope = rememberCoroutineScope()

  val addParams by viewModel.addParams.collectAsState()
  val deleteParams by viewModel.deleteParams.collectAsState()

  MountHooks(
      viewModel = viewModel,
  )

  BackHandler(
      onBack = onDismiss,
  )

  RepeatScreen(
      modifier = modifier,
      showActionButton = true,
      state = viewModel,
      topBar = {
        AppBar(
            state = viewModel,
            onDismiss = onDismiss,
            onSearchToggled = { viewModel.handleToggleSearch() },
            onSearchUpdated = { viewModel.handleSearchUpdated(it) },
        )
      },
      onActionButtonClicked = { viewModel.handleAddNewRepeat() },
      onRepeatClicked = { viewModel.handleEditRepeat(it) },
      onRepeatLongClicked = { viewModel.handleDeleteRepeat(it) },
      onRepeatDeleteFinalized = { viewModel.handleDeleteFinalized() },
      onRepeatRestored = { viewModel.handleRestoreDeleted(scope = scope) },
  )

  addParams?.also { p ->
    RepeatAddEntry(
        modifier = Modifier.fillUpToPortraitSize(),
        params = p,
        onDismiss = { viewModel.handleCloseAddRepeat() },
    )
  }

  deleteParams?.also { p ->
    RepeatDeleteEntry(
        modifier = Modifier.fillUpToPortraitSize(),
        params = p,
        onDismiss = { viewModel.handleCloseDeleteRepeat() },
    )
  }
}

@Composable
private fun AppBar(
    modifier: Modifier = Modifier,
    state: RepeatViewState,
    onDismiss: () -> Unit,

    // Search
    onSearchToggled: () -> Unit,
    onSearchUpdated: (String) -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.primary,
    ) {
      Spacer(
          modifier = Modifier.fillMaxWidth().statusBarsPadding(),
      )
    }

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        elevation = ZeroElevation,
        contentColor = MaterialTheme.colors.onPrimary,
        backgroundColor = MaterialTheme.colors.primary,
        navigationIcon = {
          IconButton(
              onClick = onDismiss,
          ) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back",
            )
          }
        },
        title = {
          Text(
              text = "Repeating Transactions",
          )
        },
        actions = {
          Search(
              state = state,
              onToggle = onSearchToggled,
          )
        },
    )

    SearchBar(
        state = state,
        onToggle = onSearchToggled,
        onChange = onSearchUpdated,
    )
  }
}
