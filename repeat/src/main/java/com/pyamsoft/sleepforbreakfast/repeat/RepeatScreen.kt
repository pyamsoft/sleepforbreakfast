package com.pyamsoft.sleepforbreakfast.repeat

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
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
fun RepeatScreen(
    modifier: Modifier = Modifier,
    state: RepeatViewState,
    onAddNewRepeat: () -> Unit,
    onDismiss: () -> Unit,
) {
  val scaffoldState = rememberScaffoldState()

  Scaffold(
      modifier = modifier,
      scaffoldState = scaffoldState,
      floatingActionButtonPosition = FabPosition.End,
      floatingActionButton = {
        FloatingActionButton(
            onClick = onAddNewRepeat,
        ) {
          Icon(
              imageVector = Icons.Filled.Add,
              contentDescription = "Add New Repeat",
          )
        }
      },
  ) { pv ->
    Spacer(
        modifier = Modifier.padding(pv),
    )
  }
}
