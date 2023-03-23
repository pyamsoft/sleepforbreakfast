package com.pyamsoft.sleepforbreakfast.sources.delete

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.money.delete.DeleteScreen
import com.pyamsoft.sleepforbreakfast.sources.SourcesCard

@Composable
fun SourcesDeleteScreen(
    modifier: Modifier = Modifier,
    state: SourcesDeleteViewState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
  DeleteScreen(
      modifier = modifier,
      state = state,
      onDismiss = onDismiss,
      onConfirm = onConfirm,
  ) { source ->
    SourcesCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
        source = source,
    )
  }
}
