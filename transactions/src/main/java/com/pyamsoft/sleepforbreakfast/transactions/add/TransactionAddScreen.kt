package com.pyamsoft.sleepforbreakfast.transactions.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.pydroid.ui.widget.MaterialCheckable
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.ui.MoneyVisualTransformation
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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

  Surface(
      modifier = modifier,
      elevation = DialogDefaults.Elevation,
      shape = MaterialTheme.shapes.medium,
  ) {
    Column {
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
          Name(
              modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
              state = state,
              keyboardTextOptions = keyboardTextOptions,
              onNameChanged = onNameChanged,
          )
        }

        item {
          Amount(
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
          Type(
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
          Note(
              modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
              state = state,
              onNoteChanged = onNoteChanged,
          )
        }

        item {
          Submit(
              modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
              state = state,
              onReset = onReset,
              onSubmit = onSubmit,
          )
        }
      }
    }
  }
}

// This is basically longer than any expected money amount.
private const val MAX_ALLOWED_AMOUNT_LENGTH = 18
private val REGEX_FILTER_ONLY_DIGITS = Regex("[^\\d+]")

@Composable
private fun Amount(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    keyboardNumberOptions: KeyboardOptions,
    onAmountChanged: (Long) -> Unit,
) {
  val amount by state.amount.collectAsState()
  val type by state.type.collectAsState()

  val amountTextValue =
      remember(amount) {
        if (amount <= 0) TextFieldValue("")
        else {
          val text = "$amount"
          TextFieldValue(
              text = text,
              // enforce the cursor at the end of the text field
              selection = TextRange(text.length),
          )
        }
      }

  val handleAmountChanged by rememberUpdatedState { tv: TextFieldValue ->
    // Ignore anything not a number when parsing to number
    val aa = tv.text.trim().replace(REGEX_FILTER_ONLY_DIGITS, "")
    if (aa.length <= MAX_ALLOWED_AMOUNT_LENGTH) {
      val newAmount = aa.toLongOrNull()

      // If this is null, we reset to 0 (in cases where a user backspaces all the content)
      onAmountChanged(newAmount ?: 0L)
    }
  }

  val typeDescription =
      remember(type) {
        when (type) {
          DbTransaction.Type.SPEND -> "Money Spent"
          DbTransaction.Type.EARN -> "Money Earned"
        }
      }
  val typeIcon =
      remember(type) {
        when (type) {
          DbTransaction.Type.SPEND -> Icons.Filled.Warning
          DbTransaction.Type.EARN -> Icons.Filled.Add
        }
      }

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
        modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
        imageVector = typeIcon,
        contentDescription = typeDescription,
    )
    TextField(
        modifier = Modifier.weight(1F),
        value = amountTextValue,
        onValueChange = { handleAmountChanged(it) },
        visualTransformation = MoneyVisualTransformation(),
        keyboardOptions = keyboardNumberOptions,
        label = {
          Text(
              text = "Amount",
          )
        },
        placeholder = {
          Text(
              text = "$0.00",
          )
        },
    )
  }
}

@Composable
private fun Type(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    onTypeChanged: (DbTransaction.Type) -> Unit,
) {
  val type by state.type.collectAsState()

  val isSpend = remember(type) { type == DbTransaction.Type.SPEND }
  val isEarn = remember(type) { type == DbTransaction.Type.EARN }

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    MaterialCheckable(
        modifier = Modifier.weight(1F),
        isEditable = true,
        condition = isSpend,
        title = "Spend",
        description = "Money Spent",
        onClick = { onTypeChanged(DbTransaction.Type.SPEND) },
    )

    Spacer(
        modifier = Modifier.width(MaterialTheme.keylines.content),
    )

    MaterialCheckable(
        modifier = Modifier.weight(1F),
        isEditable = true,
        condition = isEarn,
        title = "Earn",
        description = "Money Earned",
        onClick = { onTypeChanged(DbTransaction.Type.EARN) },
    )
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
  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    Date(
        modifier = Modifier.weight(1F),
        state = state,
        onDateChanged = onDateChanged,
        onCloseDateDialog = onCloseDateDialog,
        onOpenDateDialog = onOpenDateDialog,
    )

    Spacer(
        modifier = Modifier.width(MaterialTheme.keylines.baseline),
    )

    Time(
        modifier = Modifier.weight(1F),
        state = state,
        onTimeChanged = onTimeChanged,
        onCloseTimeDialog = onCloseTimeDialog,
        onOpenTimeDialog = onOpenTimeDialog,
    )
  }
}

private val DATE_FORMATTER =
    object : ThreadLocal<DateTimeFormatter>() {

      override fun initialValue(): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
      }
    }

private val TIME_FORMATTER =
    object : ThreadLocal<DateTimeFormatter>() {

      override fun initialValue(): DateTimeFormatter {
        return DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT)
      }
    }

@Composable
private fun Date(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    onOpenDateDialog: () -> Unit,
    onCloseDateDialog: () -> Unit,
    onDateChanged: (LocalDate) -> Unit,
) {
  val date by state.date.collectAsState()
  val showDialog by state.isDateDialogOpen.collectAsState()

  val justDate = remember(date) { DATE_FORMATTER.get().requireNotNull().format(date) }

  Text(
      modifier = modifier.clickable { onOpenDateDialog() },
      text = justDate,
      style = MaterialTheme.typography.h6,
      textAlign = TextAlign.Center,
  )

  if (showDialog) {
    DatePickerDialog(
        initialDate = date,
        onDateSelected = onDateChanged,
        onDismiss = onCloseDateDialog,
    )
  }
}

@Composable
private fun Time(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    onOpenTimeDialog: () -> Unit,
    onCloseTimeDialog: () -> Unit,
    onTimeChanged: (LocalTime) -> Unit,
) {
  val date by state.date.collectAsState()
  val showDialog by state.isTimeDialogOpen.collectAsState()

  val justTime = remember(date) { TIME_FORMATTER.get().requireNotNull().format(date) }

  Text(
      modifier = modifier.clickable { onOpenTimeDialog() },
      text = justTime,
      style = MaterialTheme.typography.h6,
      textAlign = TextAlign.Center,
  )

  if (showDialog) {
    TimePickerDialog(
        initialTime = date,
        onTimeSelected = onTimeChanged,
        onDismiss = onCloseTimeDialog,
    )
  }
}

@Composable
private fun Submit(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
) {
  val working by state.working.collectAsState()
  val isButtonEnabled = remember(working) { !working }

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    OutlinedButton(
        modifier = Modifier.weight(1F),
        onClick = onReset,
        shape =
            MaterialTheme.shapes.small.copy(
                bottomEnd = ZeroCornerSize,
                topEnd = ZeroCornerSize,
            ),
        enabled = isButtonEnabled,
    ) {
      Text(
          text = "Reset",
      )
    }
    Button(
        modifier = Modifier.weight(1F),
        onClick = onSubmit,
        enabled = isButtonEnabled,
        shape =
            MaterialTheme.shapes.small.copy(
                bottomStart = ZeroCornerSize,
                topStart = ZeroCornerSize,
            ),
        elevation = null,
    ) {
      Crossfade(
          targetState = working,
      ) { w ->
        if (w) {
          CircularProgressIndicator(
              modifier = Modifier.size(16.dp),
          )
        } else {
          Text(
              text = "Submit",
          )
        }
      }
    }
  }
}

@Composable
private fun Name(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    keyboardTextOptions: KeyboardOptions,
    onNameChanged: (String) -> Unit,
) {
  val name by state.name.collectAsState()

  TextField(
      modifier = modifier,
      value = name,
      onValueChange = onNameChanged,
      keyboardOptions = keyboardTextOptions,
      label = {
        Text(
            text = "Name",
        )
      },
      placeholder = {
        Text(
            text = "A memorable name",
        )
      },
  )
}

@Composable
private fun Note(
    modifier: Modifier = Modifier,
    state: TransactionAddViewState,
    onNoteChanged: (String) -> Unit,
) {
  val note by state.note.collectAsState()

  TextField(
      modifier = modifier,
      value = note,
      onValueChange = onNoteChanged,
      maxLines = 4,
  )
}
