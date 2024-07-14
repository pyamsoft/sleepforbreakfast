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

package com.pyamsoft.sleepforbreakfast.category.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.godaddy.android.colorpicker.ClassicColorPicker
import com.godaddy.android.colorpicker.HsvColor
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.category.CategoryColor
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryColor
import com.pyamsoft.sleepforbreakfast.money.add.AddName
import com.pyamsoft.sleepforbreakfast.money.add.AddNote
import com.pyamsoft.sleepforbreakfast.money.add.AddSubmit
import com.pyamsoft.sleepforbreakfast.ui.CardDialog

private enum class ContentTypes {
  NAME,
  NOTE,
  SUBMIT,
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun CategoryAddScreen(
    modifier: Modifier = Modifier,
    state: CategoryAddViewState,
    onNameChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onColorChanged: (Color) -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
    onOpenColorPicker: () -> Unit,
    onCloseColorPicker: () -> Unit,
) {
  val name by state.name.collectAsStateWithLifecycle()
  val note by state.note.collectAsStateWithLifecycle()
  val working by state.working.collectAsStateWithLifecycle()

  val showColorPicker by state.showColorPicker.collectAsStateWithLifecycle()

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

            CategoryColor(
                modifier =
                    Modifier.padding(start = MaterialTheme.keylines.content).size(48.dp).clickable {
                      onOpenColorPicker()
                    },
                color = LocalCategoryColor.current,
            )
          }
        }

        item(
            contentType = ContentTypes.NOTE,
        ) {
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

    if (showColorPicker) {
      ColorPickerDialog(
          onDismiss = onCloseColorPicker,
          onColorChanged = onColorChanged,
      )
    }
  }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ColorPickerDialog(
    modifier: Modifier = Modifier,
    onColorChanged: (Color) -> Unit,
    onDismiss: () -> Unit,
) {
  val defaultColor = LocalCategoryColor.current
  val (renderKey, setRenderKey) = remember { mutableStateOf(IdGenerator.generate()) }
  val (currentColor, setCurrentColor) = remember { mutableStateOf(defaultColor) }

  val handleReset by rememberUpdatedState {
    setCurrentColor(defaultColor)
    setRenderKey(IdGenerator.generate())
  }

  val handleSubmit by rememberUpdatedState {
    onColorChanged(currentColor)
    onDismiss()
  }

  val color = remember(currentColor) { HsvColor.from(currentColor) }

  CardDialog(
      modifier = modifier,
      onDismiss = onDismiss,
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
          title = {
            Text(
                text = "Color Picker",
            )
          },
      )

      // This must remount each time the color changes from being reset or it will not
      // update the new color in the picker
      Crossfade(
          targetState = renderKey,
          label = "Color Picker",
      ) { key ->
        ClassicColorPicker(
            modifier = Modifier.fillMaxHeight(fraction = 0.4F).layoutId("color-picker-$key"),
            showAlphaBar = false,
            color = color,
            onColorChanged = { setCurrentColor(it.toColor()) },
        )
      }
      Row(
          modifier = Modifier.padding(horizontal = MaterialTheme.keylines.content),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Spacer(
            modifier = Modifier.weight(1F),
        )

        TextButton(
            onClick = { handleReset() },
        ) {
          Text(
              text = "Reset",
          )
        }

        Button(
            modifier = Modifier.padding(start = MaterialTheme.keylines.content),
            onClick = { handleSubmit() },
        ) {
          Text(
              text = "Save",
          )
        }
      }
    }
  }
}
