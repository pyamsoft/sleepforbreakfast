package com.pyamsoft.sleepforbreakfast.ui

import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState

@Composable
fun <T : Any> DeletedSnackbar(
    scaffoldState: ScaffoldState,
    deleted: T?,
    onSnackbarDismissed: () -> Unit,
    onSnackbarAction: () -> Unit,
    deletedMessage: (T) -> String,
) {
  val handleMessage by rememberUpdatedState(deletedMessage)

  deleted?.also { u ->
    LaunchedEffect(u) {
      val snackbarResult =
          scaffoldState.snackbarHostState.showSnackbar(
              message = handleMessage(u),
              duration = SnackbarDuration.Short,
              actionLabel = "Undo",
          )

      when (snackbarResult) {
        SnackbarResult.Dismissed -> {
          onSnackbarDismissed()
        }
        SnackbarResult.ActionPerformed -> {
          onSnackbarAction()
        }
      }
    }
  }
}
