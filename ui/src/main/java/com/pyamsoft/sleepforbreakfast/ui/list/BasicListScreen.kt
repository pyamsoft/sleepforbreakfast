package com.pyamsoft.sleepforbreakfast.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines

@Composable
fun <T : Any> BasicListScreen(
    modifier: Modifier = Modifier,
    recentlyDeletedItem: T?,
    onActionButtonClicked: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    onSnackbarAction: () -> Unit,
    deletedMessage: (T) -> String,
    content: @Composable (PaddingValues) -> Unit,
) {
  val scaffoldState = rememberScaffoldState()

  Scaffold(
      modifier = modifier,
      scaffoldState = scaffoldState,
      floatingActionButtonPosition = FabPosition.End,
      floatingActionButton = {
        FloatingActionButton(
            onClick = onActionButtonClicked,
        ) {
          Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "Add New",
          )
        }
      },
  ) { pv ->
    content(pv)

    DeletedSnackbar(
        scaffoldState = scaffoldState,
        deleted = recentlyDeletedItem,
        onSnackbarDismissed = onSnackbarDismissed,
        onSnackbarAction = onSnackbarAction,
        deletedMessage = deletedMessage,
    )
  }
}

@Composable
fun <T : Any> ListScreen(
    modifier: Modifier = Modifier,
    items: List<T>,
    recentlyDeletedItem: T?,
    onActionButtonClicked: () -> Unit,
    onSnackbarDismissed: () -> Unit,
    onSnackbarAction: () -> Unit,
    itemKey: (T) -> String,
    deletedMessage: (T) -> String,
    item: @Composable (T) -> Unit,
) {
  BasicListScreen(
      modifier = modifier,
      recentlyDeletedItem = recentlyDeletedItem,
      onActionButtonClicked = onActionButtonClicked,
      onSnackbarDismissed = onSnackbarDismissed,
      onSnackbarAction = onSnackbarAction,
      deletedMessage = deletedMessage,
  ) { pv ->
    Column {
      Spacer(
          modifier = Modifier.padding(pv).statusBarsPadding(),
      )

      LazyColumn {
        items(
            items = items,
            key = itemKey,
        ) {
          item(it)
        }

        item {
          Spacer(
              modifier =
                  Modifier.padding(pv)
                      // Space to offset the FAB
                      .height(MaterialTheme.keylines.content * 4),
          )
        }
      }
    }
  }
}
