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

package com.pyamsoft.sleepforbreakfast.money.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.widget.MaterialCheckable
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.ui.DatePickerDialog
import com.pyamsoft.sleepforbreakfast.ui.TimePickerDialog
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
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
    state: MoneyAddViewState,
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
    state: MoneyAddViewState,
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
    state: MoneyAddViewState,
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
    state: MoneyAddViewState,
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
    state: MoneyAddViewState,
    onNoteChanged: (String) -> Unit,
    label: (@Composable () -> Unit)? = null
) {
  val note by state.note.collectAsState()
  AddNote(
      modifier = modifier,
      note = note,
      onNoteChanged = onNoteChanged,
      label = label,
  )
}

@Composable
fun AddNote(
    modifier: Modifier = Modifier,
    note: String,
    onNoteChanged: (String) -> Unit,
    label: (@Composable () -> Unit)? = null
) {
  TextField(
      modifier = modifier,
      value = note,
      onValueChange = onNoteChanged,
      maxLines = 4,
      label = label,
  )
}

@Composable
private fun Category(
    modifier: Modifier = Modifier,
    category: DbCategory,
) {
  Text(
      modifier =
          modifier
              .background(
                  color = MaterialTheme.colors.secondary,
                  shape = MaterialTheme.shapes.small,
              )
              .padding(horizontal = MaterialTheme.keylines.baseline)
              .padding(vertical = MaterialTheme.keylines.typography),
      text = category.name,
      style =
          MaterialTheme.typography.caption.copy(
              color =
                  MaterialTheme.colors.onSecondary.copy(
                      alpha = ContentAlpha.high,
                  ),
          ),
  )
}

@Composable
fun MoneyCategories(
    modifier: Modifier = Modifier,
    state: MoneyAddViewState,
    allCategories: List<DbCategory>,
    onCategoryAdded: (DbCategory) -> Unit,
    onCategoryRemoved: (DbCategory) -> Unit,
) {
  val categories by state.categories.collectAsState()
  AddCategories(
      modifier = modifier,
      selectedCategories = categories,
      allCategories = allCategories,
      onCategoryAdded = onCategoryAdded,
      onCategoryRemoved = onCategoryRemoved,
  )
}

@Composable
fun AddCategories(
    modifier: Modifier = Modifier,
    selectedCategories: List<DbCategory.Id>,
    allCategories: List<DbCategory>,
    onCategoryAdded: (DbCategory) -> Unit,
    onCategoryRemoved: (DbCategory) -> Unit,
) {
  val (show, setShow) = remember { mutableStateOf(false) }

  Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    for (id in selectedCategories) {
      // If a category is delete but still "attached" to the Transaction or Repeat, it will be nully
      // here as it won't be in the allCategories, so hide it
      val maybeCategory =
          remember(
              id,
              allCategories,
          ) {
            allCategories.firstOrNull { it.id == id }
          }
              ?: continue

      Category(
          modifier = Modifier.padding(end = MaterialTheme.keylines.baseline),
          category = maybeCategory,
      )
    }
  }

  DropdownMenu(
      properties = remember { PopupProperties(focusable = true) },
      expanded = show,
      onDismissRequest = { setShow(false) },
  ) {
    for (cat in allCategories) {
      // If this category is selected
      val isSelected =
          remember(
              cat,
              selectedCategories,
          ) {
            selectedCategories.firstOrNull { it == cat.id } != null
          }

      DropdownMenuItem(
          onClick = {
            if (isSelected) {
              onCategoryRemoved(cat)
            } else {
              onCategoryAdded(cat)
            }
          },
      ) {
        if (isSelected) {
          Category(
              category = cat,
          )
        } else {
          Text(
              text = cat.name,
          )
        }
      }
    }
  }
}
