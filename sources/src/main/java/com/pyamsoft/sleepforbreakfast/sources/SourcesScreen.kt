package com.pyamsoft.sleepforbreakfast.sources

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.FabPosition
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun SourcesScreen(
    modifier: Modifier = Modifier,
    state: SourcesViewState,
    onAddNewSource: () -> Unit,
    onDismiss: () -> Unit,
) {
  val scaffoldState = rememberScaffoldState()

  Scaffold(
      modifier = modifier,
      scaffoldState = scaffoldState,
      floatingActionButtonPosition = FabPosition.End,
      floatingActionButton = {
        FloatingActionButton(
            onClick = onAddNewSource,
        ) {
          Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "Add New Source",
          )
        }
      },
  ) { pv ->
    Spacer(
        modifier = Modifier.padding(pv).statusBarsPadding(),
    )
  }
}
