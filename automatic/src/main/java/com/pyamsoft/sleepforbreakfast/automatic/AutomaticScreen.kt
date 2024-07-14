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

package com.pyamsoft.sleepforbreakfast.automatic

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.ui.list.ListScreen

private enum class ContentTypes {
  AUTOMATIC
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun AutomaticScreen(
    modifier: Modifier = Modifier,
    state: AutomaticViewState,
    showActionButton: Boolean,
    onActionButtonClicked: () -> Unit,
    onAutomaticClicked: (DbNotificationWithRegexes) -> Unit,
    onAutomaticRestored: () -> Unit,
    onAutomaticDeleteFinalized: () -> Unit,
    topBar: @Composable () -> Unit,
    onAutomaticLongClicked: ((DbNotificationWithRegexes) -> Unit)? = null,
) {
  val loading by state.loadingState.collectAsStateWithLifecycle()
  val categories = state.items.collectAsStateListWithLifecycle()
  val undoable by state.recentlyDeleted.collectAsStateWithLifecycle()

  ListScreen(
      modifier = modifier,
      loading = loading,
      showActionButton = showActionButton,
      topBar = topBar,
      items = categories,
      recentlyDeletedItem = undoable,
      itemKey = { it.notification.id.raw },
      itemContentType = { ContentTypes.AUTOMATIC },
      deletedMessage = { "TODO delete" },
      onActionButtonClicked = onActionButtonClicked,
      onSnackbarAction = onAutomaticRestored,
      onSnackbarDismissed = onAutomaticDeleteFinalized,
  ) { automatic ->
    // TODO
    Card(
        modifier =
            Modifier.padding(horizontal = MaterialTheme.keylines.content)
                .padding(bottom = MaterialTheme.keylines.content)
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { onAutomaticClicked(automatic) },
                    onLongClick = { onAutomaticLongClicked?.invoke(automatic) },
                ),
        elevation = CardDefaults.elevatedCardElevation(),
        colors = CardDefaults.elevatedCardColors(),
        shape = MaterialTheme.shapes.medium,
    ) {
      Column(
          modifier = Modifier.padding(MaterialTheme.keylines.content),
      ) {
        Text(
            modifier = Modifier.padding(bottom = MaterialTheme.keylines.content),
            text = automatic.notification.name,
            style = MaterialTheme.typography.titleSmall,
        )

        automatic.matchRegexes.forEach { reg ->
          Text(
              modifier = Modifier.padding(bottom = MaterialTheme.keylines.typography),
              text = reg.text,
              style =
                  MaterialTheme.typography.bodyMedium.copy(
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                  ),
          )
        }
      }
    }
  }
}
