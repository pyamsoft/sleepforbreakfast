package com.pyamsoft.sleepforbreakfast.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.home.repeats.HomeOpenRepeats
import com.pyamsoft.sleepforbreakfast.home.sources.HomeOpenSources
import com.pyamsoft.sleepforbreakfast.home.transactions.HomeOpenTransactions

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    appName: String,
    onOpenSettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenRepeats: () -> Unit,
    onOpenSources: () -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    HomeHeader(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = MaterialTheme.keylines.content)
                .statusBarsPadding(),
        appName = appName,
        onOpenSettings = onOpenSettings,
    )

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
            onOpen = onOpenTransactions,
        )
      }

      item {
        HomeOpenRepeats(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
            onOpen = onOpenRepeats,
        )
      }
      item {
        HomeOpenSources(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
            onOpen = onOpenSources,
        )
      }
    }
  }
}
