/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.category.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.money.add.AddName
import com.pyamsoft.sleepforbreakfast.money.add.AddNote
import com.pyamsoft.sleepforbreakfast.money.add.AddSubmit

@Composable
fun CategoryAddScreen(
    modifier: Modifier = Modifier,
    state: CategoryAddViewState,
    onNameChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
  val name by state.name.collectAsState()
  val note by state.note.collectAsState()
  val working by state.working.collectAsState()

  val keyboardTextOptions = remember {
    KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Next,
    )
  }

  Column(
      modifier = modifier,
  ) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
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
      item {
        AddName(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            name = name,
            keyboardTextOptions = keyboardTextOptions,
            onNameChanged = onNameChanged,
        )
      }

      item {
        AddNote(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            note = note,
            onNoteChanged = onNoteChanged,
            label = {
              Text(
                  text = "Note about this Category",
              )
            },
        )
      }

      item {
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
