package com.pyamsoft.sleepforbreakfast.repeat.delete

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.money.delete.DeleteScreen
import com.pyamsoft.sleepforbreakfast.repeat.RepeatCard

@Composable
fun RepeatDeleteScreen(
    modifier: Modifier = Modifier,
    state: RepeatDeleteViewState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
  DeleteScreen(
      modifier = modifier,
      state = state,
      onDismiss = onDismiss,
      onConfirm = onConfirm,
  ) { repeat ->
    RepeatCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
        repeat = repeat,
    )
  }
}
