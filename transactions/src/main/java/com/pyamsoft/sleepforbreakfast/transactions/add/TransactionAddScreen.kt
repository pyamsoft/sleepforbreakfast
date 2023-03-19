package com.pyamsoft.sleepforbreakfast.transactions.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
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
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.DatePicker
import com.pyamsoft.sleepforbreakfast.money.MoneyAmount
import com.pyamsoft.sleepforbreakfast.money.MoneyName
import com.pyamsoft.sleepforbreakfast.money.MoneyNote
import com.pyamsoft.sleepforbreakfast.money.MoneySubmit
import com.pyamsoft.sleepforbreakfast.money.MoneyType
import com.pyamsoft.sleepforbreakfast.money.TimePicker
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colors.primary,
    ) {
      Spacer(
          modifier = Modifier.fillMaxWidth().statusBarsPadding(),
      )
    }

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
            onDateChanged = onDateChanged,
            onCloseDateDialog = onCloseDateDialog,
            onOpenDateDialog = onOpenDateDialog,
            onTimeChanged = onTimeChanged,
            onCloseTimeDialog = onCloseTimeDialog,
            onOpenTimeDialog = onOpenTimeDialog,
        )
      }

      item {
        MoneyNote(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            state = state,
            onNoteChanged = onNoteChanged,
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
private fun DateTime(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    onOpenDateDialog: () -> Unit,
    onCloseDateDialog: () -> Unit,
    onDateChanged: (LocalDate) -> Unit,
    onOpenTimeDialog: () -> Unit,
    onCloseTimeDialog: () -> Unit,
    onTimeChanged: (LocalTime) -> Unit,
) {
  val date by state.date.collectAsState()
  val showDateDialog by state.isDateDialogOpen.collectAsState()
  val showTimeDialog by state.isTimeDialogOpen.collectAsState()

  val justDate = remember(date) { date.toLocalDate() }
  val justTime = remember(date) { date.toLocalTime() }

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    DatePicker(
        modifier = Modifier.weight(1F),
        date = justDate,
        isOpen = showDateDialog,
        onDateChanged = onDateChanged,
        onCloseDateDialog = onCloseDateDialog,
        onOpenDateDialog = onOpenDateDialog,
    )

    Spacer(
        modifier = Modifier.width(MaterialTheme.keylines.baseline),
    )

    TimePicker(
        modifier = Modifier.weight(1F),
        time = justTime,
        isOpen = showTimeDialog,
        onTimeChanged = onTimeChanged,
        onCloseTimeDialog = onCloseTimeDialog,
        onOpenTimeDialog = onOpenTimeDialog,
    )
  }
}
