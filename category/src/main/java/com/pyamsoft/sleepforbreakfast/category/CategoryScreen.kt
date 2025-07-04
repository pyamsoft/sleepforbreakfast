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

package com.pyamsoft.sleepforbreakfast.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.list.Search
import com.pyamsoft.sleepforbreakfast.money.list.SearchBar
import com.pyamsoft.sleepforbreakfast.ui.ScreenTopBar
import com.pyamsoft.sleepforbreakfast.ui.list.ListScreen

private enum class ContentTypes {
  CATEGORY
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CategoryScreen(
    modifier: Modifier = Modifier,
    state: CategoryViewState,

    // App Bar
    onBack: () -> Unit,
    onSearchToggled: () -> Unit,
    onSearchUpdated: (String) -> Unit,

    // Fab
    showActionButton: Boolean,
    onActionButtonClicked: () -> Unit,

    // Item
    onCategoryClicked: (DbCategory) -> Unit,
    onCategoryRestored: () -> Unit,
    onCategoryDeleteFinalized: () -> Unit,
    onCategoryLongClicked: ((DbCategory) -> Unit)? = null,
) {
  val loading by state.loadingState.collectAsStateWithLifecycle()
  val categories = state.items.collectAsStateListWithLifecycle()
  val undoable by state.recentlyDeleted.collectAsStateWithLifecycle()

  ListScreen(
      modifier = modifier,
      loading = loading,
      showActionButton = showActionButton,
      items = categories,
      recentlyDeletedItem = undoable,
      itemKey = { it.id.raw },
      itemContentType = { ContentTypes.CATEGORY },
      deletedMessage = { "${it.name} Removed" },
      onActionButtonClicked = onActionButtonClicked,
      onSnackbarAction = onCategoryRestored,
      onSnackbarDismissed = onCategoryDeleteFinalized,
      topBar = {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onPrimary,
        ) {
          ScreenTopBar(
              onDismiss = onBack,
              title = {
                Text(
                    text = "All Categories",
                )
              },
              actions = {
                Search(
                    state = state,
                    onToggle = onSearchToggled,
                )
              },
          ) {
            SearchBar(
                state = state,
                onToggle = onSearchToggled,
                onChange = onSearchUpdated,
            )
          }
        }
      }) { category ->
        CategoryCard(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = MaterialTheme.keylines.content)
                    .padding(bottom = MaterialTheme.keylines.content),
            contentModifier =
                onCategoryLongClicked.let { longClick ->
                  if (longClick == null) {
                    Modifier.clickable { onCategoryClicked(category) }
                  } else {
                    Modifier.combinedClickable(
                        onClick = { onCategoryClicked(category) },
                        onLongClick = { longClick(category) },
                    )
                  }
                },
            category = category,
        )
      }
}
