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

package com.pyamsoft.sleepforbreakfast.repeat.add

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.PopupProperties
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.add.DatePicker
import com.pyamsoft.sleepforbreakfast.money.add.MoneyAmount
import com.pyamsoft.sleepforbreakfast.money.add.MoneyCategories
import com.pyamsoft.sleepforbreakfast.money.add.MoneyName
import com.pyamsoft.sleepforbreakfast.money.add.MoneyNote
import com.pyamsoft.sleepforbreakfast.money.add.MoneySubmit
import com.pyamsoft.sleepforbreakfast.money.add.MoneyType
import java.time.LocalDate

@Composable
fun RepeatAddScreen(
    modifier: Modifier = Modifier,
    state: RepeatAddViewState,
    onNameChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onAmountChanged: (Long) -> Unit,
    onDateChanged: (LocalDate) -> Unit,
    onOpenDateDialog: () -> Unit,
    onCloseDateDialog: () -> Unit,
    onTypeChanged: (DbTransaction.Type) -> Unit,
    onRepeatTypeChanged: (DbRepeat.Type) -> Unit,
    onCategoryAdded: (DbCategory) -> Unit,
    onCategoryRemoved: (DbCategory) -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
  val keyboardNumberOptions = remember {
    KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Decimal,
        imeAction = ImeAction.Next,
    )
  }

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
        MoneyName(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            state = state,
            keyboardTextOptions = keyboardTextOptions,
            onNameChanged = onNameChanged,
        )
      }

      item {
        MoneyAmount(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = MaterialTheme.keylines.content)
                    .padding(bottom = MaterialTheme.keylines.content * 2),
            state = state,
            keyboardNumberOptions = keyboardNumberOptions,
            onAmountChanged = onAmountChanged,
        )
      }

      item {
        MoneyType(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = MaterialTheme.keylines.content)
                    .padding(bottom = MaterialTheme.keylines.content),
            state = state,
            onTypeChanged = onTypeChanged,
        )
      }

      item {
        val firstDate by state.repeatFirstDay.collectAsState()
        val showDateDialog by state.isDateDialogOpen.collectAsState()

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.content),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          DatePicker(
              modifier = Modifier.weight(1F),
              date = firstDate,
              isOpen = showDateDialog,
              onDateChanged = onDateChanged,
              onCloseDateDialog = onCloseDateDialog,
              onOpenDateDialog = onOpenDateDialog,
          )

          Spacer(
              modifier = Modifier.width(MaterialTheme.keylines.content),
          )

          RepeatType(
              modifier = Modifier.weight(1F),
              state = state,
              onTypeChanged = onRepeatTypeChanged,
          )
        }
      }

      item {
        MoneyNote(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            state = state,
            onNoteChanged = onNoteChanged,
            label = {
              Text(
                  text = "Note about this Repeating Transaction",
              )
            },
        )
      }

      item {
        val allCategories by state.allCategories.collectAsState()

        MoneyCategories(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = MaterialTheme.keylines.content)
                    .padding(bottom = MaterialTheme.keylines.content),
            state = state,
            allCategories = allCategories,
            onCategoryAdded = onCategoryAdded,
            onCategoryRemoved = onCategoryRemoved,
        )
      }

      item {
        MoneySubmit(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            state = state,
            onReset = onReset,
            onSubmit = onSubmit,
        )
      }
    }
  }
}

@Composable
private fun RepeatType(
    modifier: Modifier = Modifier,
    state: RepeatAddViewState,
    onTypeChanged: (DbRepeat.Type) -> Unit,
) {
  val repeatType by state.repeatType.collectAsState()

  // TODO move into VM
  val (show, setShow) = rememberSaveable { mutableStateOf(false) }

  val handleShow by rememberUpdatedState { setShow(true) }
  val allPossible = remember { DbRepeat.Type.values() }

  Text(
      modifier = modifier.clickable { handleShow() },
      text = repeatType.displayName,
      style = MaterialTheme.typography.body1,
  )

  DropdownMenu(
      properties = remember { PopupProperties(focusable = true) },
      expanded = show,
      onDismissRequest = { setShow(false) },
  ) {
    for (type in allPossible) {
      // If this category is selected
      val isSelected =
          remember(
              repeatType,
              type,
          ) {
            repeatType == type
          }

      DropdownMenuItem(
          onClick = {
            if (!isSelected) {
              onTypeChanged(type)
            }
          },
      ) {
        Text(
            text = type.displayName,
        )
      }
    }
  }
}
