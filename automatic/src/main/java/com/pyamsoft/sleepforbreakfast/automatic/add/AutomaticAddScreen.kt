/*
 * Copyright 2025 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.automatic.add

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.money.add.AddName
import com.pyamsoft.sleepforbreakfast.money.add.AddSubmit

private enum class ContentTypes {
  NAME,
  SUBMIT,
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun AutomaticAddScreen(
    modifier: Modifier = Modifier,
    state: AutomaticAddViewState,
    onNameChanged: (String) -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
  val name by state.name.collectAsStateWithLifecycle()
  val working by state.working.collectAsStateWithLifecycle()

  val keyboardTextOptions = remember {
    KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Next,
    )
  }

  Box(
      modifier = modifier,
      contentAlignment = Alignment.Center,
  ) {
    Column {
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
          title = {},
      )

      LazyColumn {
        item(
            contentType = ContentTypes.NAME,
        ) {
          Row(
              modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
              verticalAlignment = Alignment.CenterVertically,
          ) {
            AddName(
                modifier = Modifier.weight(1F),
                name = name,
                keyboardTextOptions = keyboardTextOptions,
                onNameChanged = onNameChanged,
            )
          }
        }

        item(
            contentType = ContentTypes.SUBMIT,
        ) {
          AddSubmit(
              modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
              working = working,
              onReset = onReset,
              onSubmit = onSubmit,
          )
        }
      }
    }
  }
}
