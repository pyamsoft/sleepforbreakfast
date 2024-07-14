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

package com.pyamsoft.sleepforbreakfast.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.ui.Delayed
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import com.pyamsoft.sleepforbreakfast.ui.renderPYDroidExtras

@Composable
fun <T : Any> BasicListScreen(
    modifier: Modifier = Modifier,
    loading: LoadingState,
    showActionButton: Boolean,
    actionButtonBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    recentlyDeletedItem: T?,
    onActionButtonClicked: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    onSnackbarAction: () -> Unit,
    deletedMessage: (T) -> String,
    topBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit,
) {
  val isLoading = remember(loading) { loading !== LoadingState.DONE }

  val snackbarHostState = remember { SnackbarHostState() }

  Scaffold(
      modifier = modifier,
      topBar = topBar,
      floatingActionButtonPosition = FabPosition.End,
      floatingActionButton = {
        AnimatedFab(
            modifier = Modifier.navigationBarsPadding(),
            show = showActionButton,
            backgroundColor = actionButtonBackgroundColor,
            onClick = onActionButtonClicked,
        )
      },
  ) { pv ->
    Crossfade(
        label = "List Screen",
        targetState = isLoading,
    ) { isLoad ->
      if (isLoad) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
          Delayed {
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
            )
          }
        }
      } else {
        content(pv)
      }
    }

    DeletedSnackbar(
        snackbarHostState = snackbarHostState,
        deleted = recentlyDeletedItem,
        onSnackbarDismissed = onSnackbarDismissed,
        onSnackbarAction = onSnackbarAction,
        deletedMessage = deletedMessage,
    )
  }
}

@Composable
private fun AnimatedFab(
    modifier: Modifier = Modifier,
    backgroundColor: Color,
    show: Boolean,
    onClick: () -> Unit,
) {
  AnimatedVisibility(
      visible = show,
      enter = scaleIn(),
      exit = scaleOut(),
  ) {
    FloatingActionButton(
        modifier = modifier,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        onClick = onClick,
    ) {
      Icon(
          imageVector = Icons.Filled.Add,
          contentDescription = "Add New",
      )
    }
  }
}

private enum class ContentTypes {
  TOP_SPACER,
  BOTTOM_SPACER,
}

@Composable
fun <T : Any> ListScreen(
    modifier: Modifier = Modifier,
    loading: LoadingState,
    items: SnapshotStateList<T>,
    showActionButton: Boolean,
    recentlyDeletedItem: T?,
    onActionButtonClicked: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    onSnackbarAction: () -> Unit,
    itemKey: (T) -> String,
    itemContentType: (T) -> Any,
    deletedMessage: (T) -> String,
    topBar: @Composable () -> Unit = {},
    item: @Composable (T) -> Unit,
) {
  BasicListScreen(
      modifier = modifier,
      loading = loading,
      showActionButton = showActionButton,
      topBar = topBar,
      recentlyDeletedItem = recentlyDeletedItem,
      onActionButtonClicked = onActionButtonClicked,
      onSnackbarDismissed = onSnackbarDismissed,
      onSnackbarAction = onSnackbarAction,
      deletedMessage = deletedMessage,
  ) { pv ->
    LazyColumn {
      renderPYDroidExtras()

      item(
          contentType = ContentTypes.TOP_SPACER,
      ) {
        Spacer(
            modifier = Modifier.padding(pv).height(MaterialTheme.keylines.content),
        )
      }

      items(
          items = items,
          key = itemKey,
          contentType = itemContentType,
      ) {
        item(it)
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
