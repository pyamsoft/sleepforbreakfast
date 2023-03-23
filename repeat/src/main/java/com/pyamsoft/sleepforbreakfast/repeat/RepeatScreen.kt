package com.pyamsoft.sleepforbreakfast.repeat

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
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.ui.ListScreen

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun RepeatScreen(
    modifier: Modifier = Modifier,
    state: RepeatViewState,
    onAddNewRepeat: () -> Unit,
    onEditRepeat: (DbRepeat) -> Unit,
    onDeleteRepeat: (DbRepeat) -> Unit,
    onRepeatRestored: () -> Unit,
    onRepeatDeleteFinalized: () -> Unit,
    onDismiss: () -> Unit,
) {
  val sources = state.items.collectAsStateList()
  val undoable by state.recentlyDeleted.collectAsState()

  ListScreen(
      modifier = modifier,
      items = sources,
      recentlyDeletedItem = undoable,
      itemKey = { it.id.raw },
      deletedMessage = { "${it.transactionName} Removed" },
      onActionButtonClicked = onAddNewRepeat,
      onSnackbarAction = onRepeatRestored,
      onSnackbarDismissed = onRepeatDeleteFinalized,
  ) { repeat ->
    RepeatCard(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = MaterialTheme.keylines.content)
                .padding(bottom = MaterialTheme.keylines.content),
        contentModifier =
            Modifier.combinedClickable(
                onClick = { onEditRepeat(repeat) },
                onLongClick = { onDeleteRepeat(repeat) },
            ),
        repeat = repeat,
    )
  }
}
