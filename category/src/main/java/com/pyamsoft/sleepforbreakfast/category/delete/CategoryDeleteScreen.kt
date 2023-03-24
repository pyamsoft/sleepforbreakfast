package com.pyamsoft.sleepforbreakfast.category.delete

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.category.CategoryCard
import com.pyamsoft.sleepforbreakfast.money.delete.DeleteScreen

@Composable
fun CategoryDeleteScreen(
    modifier: Modifier = Modifier,
    state: CategoryDeleteViewState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
  DeleteScreen(
      modifier = modifier,
      state = state,
      onDismiss = onDismiss,
      onConfirm = onConfirm,
  ) { category ->
    CategoryCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
        category = category,
    )
  }
}
