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
