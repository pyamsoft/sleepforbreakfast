package com.pyamsoft.sleepforbreakfast.sources

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
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.ui.ListScreen

@Composable
@OptIn(ExperimentalFoundationApi::class)
fun SourcesScreen(
    modifier: Modifier = Modifier,
    state: SourcesViewState,
    onAddNewSources: () -> Unit,
    onEditSources: (DbSource) -> Unit,
    onDeleteSources: (DbSource) -> Unit,
    onSourcesRestored: () -> Unit,
    onSourcesDeleteFinalized: () -> Unit,
    onDismiss: () -> Unit,
) {
  val sources = state.items.collectAsStateList()
  val undoable by state.recentlyDeleted.collectAsState()

  ListScreen(
      modifier = modifier,
      items = sources,
      recentlyDeletedItem = undoable,
      itemKey = { it.id.raw },
      deletedMessage = { "${it.name} Removed" },
      onActionButtonClicked = onAddNewSources,
      onSnackbarAction = onSourcesRestored,
      onSnackbarDismissed = onSourcesDeleteFinalized,
  ) { source ->
    SourcesCard(
        modifier =
            Modifier.fillMaxWidth()
                .padding(horizontal = MaterialTheme.keylines.content)
                .padding(bottom = MaterialTheme.keylines.content),
        contentModifier =
            Modifier.combinedClickable(
                onClick = { onEditSources(source) },
                onLongClick = { onDeleteSources(source) },
            ),
        source = source,
    )
  }
}
