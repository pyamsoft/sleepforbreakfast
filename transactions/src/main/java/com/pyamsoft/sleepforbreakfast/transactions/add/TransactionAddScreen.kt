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

package com.pyamsoft.sleepforbreakfast.transactions.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.pydroid.ui.util.fullScreenDialog
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.add.DatePicker
import com.pyamsoft.sleepforbreakfast.money.add.MoneyAmount
import com.pyamsoft.sleepforbreakfast.money.add.MoneyCategories
import com.pyamsoft.sleepforbreakfast.money.add.MoneyName
import com.pyamsoft.sleepforbreakfast.money.add.MoneyNote
import com.pyamsoft.sleepforbreakfast.money.add.MoneySubmit
import com.pyamsoft.sleepforbreakfast.money.add.MoneyType
import com.pyamsoft.sleepforbreakfast.money.add.TimePicker
import com.pyamsoft.sleepforbreakfast.transactions.auto.TransactionAutoScreen
import com.pyamsoft.sleepforbreakfast.transactions.repeat.TransactionRepeatInfoScreen
import com.pyamsoft.sleepforbreakfast.ui.DatePickerDialog
import com.pyamsoft.sleepforbreakfast.ui.SurfaceDialog
import com.pyamsoft.sleepforbreakfast.ui.TimePickerDialog
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun TransactionAddScreen(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    onNameChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onAmountChanged: (Long) -> Unit,
    onTypeChanged: (DbTransaction.Type) -> Unit,
    onDateChanged: (LocalDate) -> Unit,
    onOpenDateDialog: () -> Unit,
    onCloseDateDialog: () -> Unit,
    onTimeChanged: (LocalTime) -> Unit,
    onOpenTimeDialog: () -> Unit,
    onCloseTimeDialog: () -> Unit,
    onCategoryAdded: (DbCategory) -> Unit,
    onCategoryRemoved: (DbCategory) -> Unit,
    onRepeatInfoOpen: () -> Unit,
    onRepeatInfoClosed: () -> Unit,
    onAutoInfoOpen: () -> Unit,
    onAutoInfoClosed: () -> Unit,
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
        elevation = ZeroElevation,
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
        DateTime(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = MaterialTheme.keylines.content)
                    .padding(bottom = MaterialTheme.keylines.content),
            state = state,
            onOpenDateDialog = onOpenDateDialog,
            onOpenTimeDialog = onOpenTimeDialog,
        )
      }

      item {
        MoneyNote(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            state = state,
            onNoteChanged = onNoteChanged,
            label = {
              Text(
                  text = "Note about this Transaction",
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
        Row(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = MaterialTheme.keylines.content)
                    .padding(bottom = MaterialTheme.keylines.content),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          RepeatInfo(
              state = state,
              onRepeatInfoOpen = onRepeatInfoOpen,
          )

          AutoInfo(
              state = state,
              onAutoInfoOpen = onAutoInfoOpen,
          )
        }
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

    val existing by state.existingTransaction.collectAsState()

    val isOpenRepeat by state.isRepeatOpen.collectAsState()
    val repeatDate = existing?.repeatCreatedDate
    if (isOpenRepeat && repeatDate != null) {
      val loadingRepeat by state.loadingRepeat.collectAsState()
      val repeat by state.existingRepeat.collectAsState()

      SurfaceDialog(
          modifier = Modifier.fullScreenDialog(),
          onDismiss = onRepeatInfoClosed,
      ) {
        TransactionRepeatInfoScreen(
            repeat = repeat,
            loading = loadingRepeat,
            date = repeatDate,
            onDismiss = onRepeatInfoClosed,
        )
      }
    }

    val loadingAuto by state.loadingAuto.collectAsState()
    val isOpenAuto by state.isAutoOpen.collectAsState()
    val auto by state.existingAuto.collectAsState()

    val autoDate = existing?.automaticCreatedDate
    if (isOpenAuto && autoDate != null) {
      SurfaceDialog(
          modifier = Modifier.fullScreenDialog(),
          onDismiss = onAutoInfoClosed,
      ) {
        TransactionAutoScreen(
            auto = auto,
            loading = loadingAuto,
            date = autoDate,
            onDismiss = onAutoInfoClosed,
        )
      }
    }

    val showDateDialog by state.isDateDialogOpen.collectAsState()
    if (showDateDialog) {
      val date by state.date.collectAsState()
      val justDate = remember(date) { date.toLocalDate() }

      DatePickerDialog(
          initialDate = justDate,
          onDateSelected = onDateChanged,
          onDismiss = onCloseDateDialog,
      )
    }

    val showTimeDialog by state.isTimeDialogOpen.collectAsState()
    if (showTimeDialog) {
      val date by state.date.collectAsState()
      val justTime = remember(date) { date.toLocalTime() }

      TimePickerDialog(
          initialTime = justTime,
          onTimeSelected = onTimeChanged,
          onDismiss = onCloseTimeDialog,
      )
    }
  }
}

@Composable
private fun RepeatInfo(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    onRepeatInfoOpen: () -> Unit,
) {
  val existing by state.existingTransaction.collectAsState()
  Crossfade(
      targetState = existing,
  ) { e ->
    if (e == null) {
      return@Crossfade
    }

    if (e.repeatId == null) {
      Text(
          modifier = modifier,
          text = "No Repeat Info",
          style = MaterialTheme.typography.body1,
      )
    } else {
      IconButton(
          modifier = modifier,
          onClick = onRepeatInfoOpen,
      ) {
        Icon(
            imageVector = Icons.Filled.AccountBox,
            contentDescription = "View Repeat Info",
        )
      }
    }
  }
}

@Composable
private fun AutoInfo(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    onAutoInfoOpen: () -> Unit,
) {
  val existing by state.existingTransaction.collectAsState()

  Crossfade(
      targetState = existing,
  ) { e ->
    if (e == null) {
      return@Crossfade
    }

    if (e.automaticId == null) {
      Text(
          modifier = modifier,
          text = "No Auto Info",
          style = MaterialTheme.typography.body1,
      )
    } else {
      IconButton(
          modifier = modifier,
          onClick = onAutoInfoOpen,
      ) {
        Icon(
            imageVector = Icons.Filled.AddCircle,
            contentDescription = "View Auto Info",
        )
      }
    }
  }
}

@Composable
private fun DateTime(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    onOpenDateDialog: () -> Unit,
    onOpenTimeDialog: () -> Unit,
) {
  val date by state.date.collectAsState()

  val justDate = remember(date) { date.toLocalDate() }
  val justTime = remember(date) { date.toLocalTime() }

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    DatePicker(
        modifier = Modifier.weight(1F),
        date = justDate,
        onOpenDateDialog = onOpenDateDialog,
    )

    Spacer(
        modifier = Modifier.width(MaterialTheme.keylines.baseline),
    )

    TimePicker(
        modifier = Modifier.weight(1F),
        time = justTime,
        onOpenTimeDialog = onOpenTimeDialog,
    )
  }
}
