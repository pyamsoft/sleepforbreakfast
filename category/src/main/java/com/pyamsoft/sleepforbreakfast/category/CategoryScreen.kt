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
  val category = state.items.collectAsStateList()
  val undoable by state.recentlyDeleted.collectAsState()

  ListScreen(
      modifier = modifier,
      items = category,
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
