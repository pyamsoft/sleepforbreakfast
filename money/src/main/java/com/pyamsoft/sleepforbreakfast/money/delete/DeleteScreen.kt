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

package com.pyamsoft.sleepforbreakfast.money.delete

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun <T : Any> DeleteScreen(
    modifier: Modifier = Modifier,
    state: DeleteViewState<T>,
    canDelete: Boolean = true,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable (T) -> Unit,
) {
  val working by state.working.collectAsStateWithLifecycle()
  val item by state.item.collectAsStateWithLifecycle()

  Column(
      modifier = modifier,
  ) {
    val contentColor = LocalContentColor.current

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                actionIconContentColor = contentColor,
                navigationIconContentColor = contentColor,
                titleContentColor = contentColor,
            ),
        title = {
          Text(
              text = "Really Remove?",
          )
        },
        navigationIcon = {
          IconButton(
              onClick = onDismiss,
          ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
            )
          }
        },
    )

    Crossfade(
        label = "Loading Remove",
        modifier = Modifier.padding(MaterialTheme.keylines.content),
        targetState = item,
    ) { data ->
      if (data == null) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      } else {
        Column {
          content(data)

          Button(
              modifier = Modifier.fillMaxWidth(),
              onClick = onConfirm,
              enabled = !working && canDelete,
          ) {
            Crossfade(
                label = "Removing",
                targetState = working,
            ) { w ->
              if (w) {
                CircularProgressIndicator()
              } else {
                Text(
                    text = "Remove",
                )
              }
            }
          }
        }
      }
    }
  }
}
