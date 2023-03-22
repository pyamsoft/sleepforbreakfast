package com.pyamsoft.sleepforbreakfast.money

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.widget.MaterialCheckable
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.ui.DatePickerDialog
import com.pyamsoft.sleepforbreakfast.ui.MoneyVisualTransformation
import com.pyamsoft.sleepforbreakfast.ui.TimePickerDialog
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

// This is basically longer than any expected money amount.
private const val MAX_ALLOWED_AMOUNT_LENGTH = 18
private val REGEX_FILTER_ONLY_DIGITS = Regex("[^\\d+]")

@Composable
fun MoneyAmount(
    modifier: Modifier = Modifier,
    state: MoneyViewState,
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
fun MoneyType(
    modifier: Modifier = Modifier,
    state: MoneyViewState,
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
fun DatePicker(
    modifier: Modifier = Modifier,
    date: LocalDate,
    isOpen: Boolean,
    onOpenDateDialog: () -> Unit,
    onCloseDateDialog: () -> Unit,
    onDateChanged: (LocalDate) -> Unit,
) {
  val justDate = remember(date) { DATE_FORMATTER.get().requireNotNull().format(date) }

  Text(
      modifier = modifier.clickable { onOpenDateDialog() },
      text = justDate,
      style = MaterialTheme.typography.h6,
      textAlign = TextAlign.Center,
  )

  if (isOpen) {
    DatePickerDialog(
        initialDate = date,
        onDateSelected = onDateChanged,
        onDismiss = onCloseDateDialog,
    )
  }
}

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    time: LocalTime,
    isOpen: Boolean,
    onOpenTimeDialog: () -> Unit,
    onCloseTimeDialog: () -> Unit,
    onTimeChanged: (LocalTime) -> Unit,
) {
  val justTime = remember(time) { TIME_FORMATTER.get().requireNotNull().format(time) }

  Text(
      modifier = modifier.clickable { onOpenTimeDialog() },
      text = justTime,
      style = MaterialTheme.typography.h6,
      textAlign = TextAlign.Center,
  )

  if (isOpen) {
    TimePickerDialog(
        initialTime = time,
        onTimeSelected = onTimeChanged,
        onDismiss = onCloseTimeDialog,
    )
  }
}

@Composable
fun MoneySubmit(
    modifier: Modifier = Modifier,
    state: MoneyViewState,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
) {
  val working by state.working.collectAsState()

  AddSubmit(
      modifier = modifier,
      working = working,
      onReset = onReset,
      onSubmit = onSubmit,
  )
}

@Composable
fun AddSubmit(
    modifier: Modifier = Modifier,
    working: Boolean,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
) {
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
fun MoneyName(
    modifier: Modifier = Modifier,
    state: MoneyViewState,
    keyboardTextOptions: KeyboardOptions,
    onNameChanged: (String) -> Unit,
) {
  val name by state.name.collectAsState()

  AddName(
      modifier = modifier,
      name = name,
      keyboardTextOptions = keyboardTextOptions,
      onNameChanged = onNameChanged,
  )
}

@Composable
fun AddName(
    modifier: Modifier = Modifier,
    name: String,
    keyboardTextOptions: KeyboardOptions,
    onNameChanged: (String) -> Unit,
) {
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
fun MoneyNote(
    modifier: Modifier = Modifier,
    state: MoneyViewState,
    onNoteChanged: (String) -> Unit,
) {
  val note by state.note.collectAsState()
  AddNote(
      modifier = modifier,
      note = note,
      onNoteChanged = onNoteChanged,
  )
}

@Composable
fun AddNote(
    modifier: Modifier = Modifier,
    note: String,
    onNoteChanged: (String) -> Unit,
) {
  TextField(
      modifier = modifier,
      value = note,
      onValueChange = onNoteChanged,
      maxLines = 4,
  )
}
