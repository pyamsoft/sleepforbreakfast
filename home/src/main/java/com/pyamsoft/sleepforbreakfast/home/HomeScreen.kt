package com.pyamsoft.sleepforbreakfast.home

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    onOpenTransactions: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
) {
  LazyColumn(
      modifier = modifier,
  ) {
    item {
      HomeOptions(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
          state = state,
          onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
      )
    }

    item {
      HomeOpenTransactions(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
          onOpenTransactions = onOpenTransactions,
      )
    }
  }
}
