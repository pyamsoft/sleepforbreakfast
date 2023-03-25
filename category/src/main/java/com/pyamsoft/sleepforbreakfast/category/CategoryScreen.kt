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

package com.pyamsoft.sleepforbreakfast.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.ui.ListScreen

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun CategoryScreen(
    modifier: Modifier = Modifier,
    state: CategoryViewState,
    onAddNewCategory: () -> Unit,
    onEditCategory: (DbCategory) -> Unit,
    onDeleteCategory: (DbCategory) -> Unit,
    onCategoryRestored: () -> Unit,
    onCategoryDeleteFinalized: () -> Unit,
    onDismiss: () -> Unit,
) {
  val categories = state.items.collectAsStateList()
  val undoable by state.recentlyDeleted.collectAsState()

  ListScreen(
      modifier = modifier,
      items = categories,
      recentlyDeletedItem = undoable,
      itemKey = { it.id.raw },
      deletedMessage = { "${it.name} Removed" },
      onActionButtonClicked = onAddNewCategory,
      onSnackbarAction = onCategoryRestored,
      onSnackbarDismissed = onCategoryDeleteFinalized,
  ) { category ->
    CategoryCard(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = MaterialTheme.keylines.content)
                .padding(bottom = MaterialTheme.keylines.content),
        contentModifier =
            Modifier.combinedClickable(
                onClick = { onEditCategory(category) },
                onLongClick = { onDeleteCategory(category) },
            ),
        category = category,
    )
  }
}
