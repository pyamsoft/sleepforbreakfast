/*
 * Copyright 2026 pyamsoft
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

@file:LintIgnoreTooManyFunctions

package com.pyamsoft.sleepforbreakfast.money.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.InputTransformation
import androidx.compose.foundation.text.input.TextFieldBuffer
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TextFieldLabelScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.core.LintIgnoreTooManyFunctions
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.pydroid.ui.util.isPortrait
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.DATE_FORMATTER
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContainerColor
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContentColor
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryObserver
import com.pyamsoft.sleepforbreakfast.money.TIME_FORMATTER
import com.pyamsoft.sleepforbreakfast.ui.complement
import com.pyamsoft.sleepforbreakfast.ui.icons.IconPainters
import java.time.LocalDate
import java.time.LocalTime

// This is basically longer than any expected money amount.
private const val MAX_ALLOWED_AMOUNT_LENGTH = 18

private val WHITESPACE_REGEX = Regex("\\s")
private val INVALID_CHAR_REGEX = Regex("[,\\-/_]")

private object AmountInputTransformation : InputTransformation {
  override fun TextFieldBuffer.transformInput() {
    val self = this
    val proposed = self.asCharSequence()

    // No long strings
    if (proposed.length >= MAX_ALLOWED_AMOUNT_LENGTH) {
      revertAllChanges()
      return
    }

    // No white space
    if (proposed.contains(WHITESPACE_REGEX)) {
      revertAllChanges()
      return
    }

    // Also don't allow other characters
    if (proposed.contains(INVALID_CHAR_REGEX)) {
      revertAllChanges()
      return
    }

    // Can't start with decimal
    if (proposed.startsWith('.')) {
      revertAllChanges()
      return
    }

    // No double decimals
    if (proposed.count { it == '.' } >= 2) {
      revertAllChanges()
      return
    }
  }
}

@Composable
fun MoneyAmount(
    modifier: Modifier = Modifier,
    state: MoneyAddViewState,
    keyboardNumberOptions: KeyboardOptions,
) {
  val amount = state.amount

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    TextField(
        modifier = Modifier.weight(1F),
        state = amount,
        keyboardOptions = keyboardNumberOptions,
        inputTransformation = AmountInputTransformation,
        leadingIcon = {
          Icon(
              painter = IconPainters.attachMoney(),
              contentDescription = "Amount",
          )
        },
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
fun MoneyTypes(
    modifier: Modifier = Modifier,
    state: MoneyAddViewState,
    onTypeChanged: (DbTransaction.Type) -> Unit,
) {
  val type by state.type.collectAsStateWithLifecycle()

  Column(
      modifier = modifier.width(IntrinsicSize.Min),
  ) {
    SpendType(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.typography),
        type = DbTransaction.Type.SPEND,
        current = type,
        onTypeChanged = onTypeChanged,
    )

    SpendType(
        modifier = Modifier.fillMaxWidth(),
        type = DbTransaction.Type.EARN,
        current = type,
        onTypeChanged = onTypeChanged,
    )
  }
}

@Composable
private fun SpendType(
    modifier: Modifier = Modifier,
    type: DbTransaction.Type,
    current: DbTransaction.Type,
    onTypeChanged: (DbTransaction.Type) -> Unit,
) {
  val shape = MaterialTheme.shapes.small
  val spendColor = MaterialTheme.colorScheme.error
  val earnColor = MaterialTheme.colorScheme.tertiary
  val color =
      remember(
          type,
          spendColor,
          earnColor,
      ) {
        when (type) {
          DbTransaction.Type.SPEND -> spendColor
          DbTransaction.Type.EARN -> earnColor
        }
      }

  val isSelected =
      remember(
          type,
          current,
      ) {
        type == current
      }

  Card(
      modifier =
          modifier.border(
              width = 2.dp,
              color = color,
              shape = shape,
          ),
      shape = shape,
      colors =
          CardDefaults.cardColors(
              containerColor = if (isSelected) color else Color.Unspecified,
          ),
  ) {
    Column(
        modifier =
            Modifier.clickable { onTypeChanged(type) }.padding(MaterialTheme.keylines.typography),
    ) {
      Text(
          modifier =
              Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.typography),
          text = type.name,
          fontWeight = FontWeight.W700,
          style = MaterialTheme.typography.bodyMedium,
          textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
fun DatePicker(
    modifier: Modifier = Modifier,
    date: LocalDate,
    onOpenDateDialog: () -> Unit,
) {
  val justDate = remember(date) { DATE_FORMATTER.format(date) }

  Text(
      modifier = modifier.clickable { onOpenDateDialog() },
      text = justDate,
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
  )
}

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    time: LocalTime,
    onOpenTimeDialog: () -> Unit,
) {
  val justTime = remember(time) { TIME_FORMATTER.format(time) }

  Text(
      modifier = modifier.clickable { onOpenTimeDialog() },
      text = justTime,
      style = MaterialTheme.typography.headlineSmall,
      textAlign = TextAlign.Center,
  )
}

@Composable
fun MoneySubmit(
    modifier: Modifier = Modifier,
    state: MoneyAddViewState,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
) {
  val working by state.working.collectAsStateWithLifecycle()

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
        colors =
            ButtonDefaults.outlinedButtonColors(
                contentColor = LocalCategoryContainerColor.current,
            ),
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
        colors =
            ButtonDefaults.buttonColors(
                containerColor = LocalCategoryContainerColor.current,
                contentColor = LocalCategoryContentColor.current,
            ),
    ) {
      Crossfade(
          label = "Submit",
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
) {
  AddName(
      modifier = modifier,
      state = state.name,
      keyboardTextOptions = keyboardTextOptions,
  )
}

@Composable
fun AddName(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    keyboardTextOptions: KeyboardOptions,
) {
  TextField(
      modifier = modifier,
      state = state,
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
    label: (@Composable TextFieldLabelScope.() -> Unit)? = null,
) {
  val note = state.note
  AddNote(
      modifier = modifier,
      state = note,
      label = label,
  )
}

@Composable
fun AddNote(
    modifier: Modifier = Modifier,
    state: TextFieldState,
    label: (@Composable TextFieldLabelScope.() -> Unit)? = null,
) {
  TextField(
      modifier = modifier,
      state = state,
      lineLimits = TextFieldLineLimits.MultiLine(4),
      label = label,
      colors = TextFieldDefaults.colors(),
  )
}

@Composable
private fun CategoryChip(
    modifier: Modifier = Modifier,
    category: DbCategory,
    isSelected: Boolean,
) {
  val defaultColor = MaterialTheme.colorScheme.secondary
  val backgroundColor =
      remember(
          isSelected,
          category,
          defaultColor,
      ) {
        if (isSelected) {
          val c = category.color
          return@remember if (c == 0L) defaultColor else Color(c.toULong())
        } else {
          return@remember Color.Unspecified
        }
      }

  val unselectedTextColor = MaterialTheme.colorScheme.onSurface
  val selectedTextColor = MaterialTheme.colorScheme.onSecondary
  val textColor =
      remember(
          category,
          isSelected,
          unselectedTextColor,
          selectedTextColor,
      ) {
        if (isSelected) {
          val c = category.color
          return@remember if (c == 0L) selectedTextColor else Color(c.toULong()).complement
        } else {
          return@remember unselectedTextColor
        }
      }
  Text(
      modifier =
          modifier
              .background(
                  color = backgroundColor,
                  shape = MaterialTheme.shapes.small,
              )
              .padding(horizontal = MaterialTheme.keylines.baseline)
              .padding(vertical = MaterialTheme.keylines.typography),
      text = category.name,
      style =
          MaterialTheme.typography.bodySmall.copy(
              color = textColor,
          ),
  )
}

@Composable
fun MoneyCategories(
    modifier: Modifier = Modifier,
    state: MoneyAddViewState,
    canAdd: Boolean,
    showLabel: Boolean,
    onCategoryAdded: ((DbCategory) -> Unit)?,
    onCategoryRemoved: ((DbCategory) -> Unit)?,
) {
  val categories = state.categories.collectAsStateListWithLifecycle()
  AddCategories(
      modifier = modifier,
      canAdd = canAdd,
      showLabel = showLabel,
      selectedCategories = categories,
      onCategoryAdded = onCategoryAdded,
      onCategoryRemoved = onCategoryRemoved,
  )
}

@Composable
fun AddCategories(
    modifier: Modifier = Modifier,
    canAdd: Boolean,
    showLabel: Boolean,
    selectedCategories: List<DbCategory.Id>,
    onCategoryAdded: ((DbCategory) -> Unit)?,
    onCategoryRemoved: ((DbCategory) -> Unit)?,
) {
  val observer = LocalCategoryObserver.current

  // TODO move into VM
  val (show, setShow) = rememberSaveable { mutableStateOf(false) }

  val handleShow by rememberUpdatedState { setShow(true) }
  val handleHide by rememberUpdatedState { setShow(false) }

  Column(
      modifier = modifier,
  ) {
    if (showLabel) {
      Text(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
          text = "Categories",
          fontWeight = FontWeight.W700,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          style = MaterialTheme.typography.bodySmall,
      )
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
    ) {
      if (canAdd) {
        Icon(
            modifier =
                Modifier.padding(end = MaterialTheme.keylines.content).clickable { handleShow() },
            painter = IconPainters.add(),
            contentDescription = "Categories",
        )
      }

      val showCategories = observer.map(selectedCategories)
      for (cat in showCategories) {
        CategoryChip(
            modifier =
                Modifier.padding(end = MaterialTheme.keylines.baseline)
                    .padding(bottom = MaterialTheme.keylines.baseline)
                    .clickable(enabled = canAdd) { handleShow() },
            category = cat,
            isSelected = true,
        )
      }
    }

    if (canAdd) {
      CategoryDropdown(
          show = show,
          selectedCategories = selectedCategories,
          onHide = { handleHide() },
          onCategoryAdded = onCategoryAdded,
          onCategoryRemoved = onCategoryRemoved,
      )
    }
  }
}

@Composable
private fun CategoryDropdown(
    modifier: Modifier = Modifier,
    show: Boolean,
    selectedCategories: List<DbCategory.Id>,
    onHide: () -> Unit,
    onCategoryAdded: ((DbCategory) -> Unit)?,
    onCategoryRemoved: ((DbCategory) -> Unit)?,
) {
  val observer = LocalCategoryObserver.current
  val configuration = LocalConfiguration.current
  val dropdownMaxHeight =
      remember(configuration) {
        val h = configuration.run {
          val size = if (isPortrait) screenHeightDp else screenWidthDp
          return@run size / 3
        }

        return@remember h.dp
      }

  val isClickEnabled =
      remember(
          onCategoryAdded,
          onCategoryRemoved,
      ) {
        onCategoryAdded != null && onCategoryRemoved != null
      }

  DropdownMenu(
      modifier = modifier.heightIn(max = dropdownMaxHeight),
      properties = remember { PopupProperties(focusable = true) },
      expanded = show,
      onDismissRequest = onHide,
  ) {

    // Remember this only on the initial composition to keep the "starting selected" on top,
    // then alphabetical. New selections will not go to the top
    val allCategories = observer.collect()
    val showCategories =
        remember(allCategories) {
          val result = mutableListOf<DbCategory>()
          val unselected = mutableListOf<DbCategory>()
          for (cat in allCategories) {
            val selected = selectedCategories.firstOrNull { it == cat.id }
            if (selected != null) {
              result.add(cat)
            } else {
              unselected.add(cat)
            }
          }

          // Sort the existing result list in place, selected on top alphabetical
          result.sortBy { it.name.lowercase() }

          // Then sort unselected, and all the two
          unselected.sortBy { it.name.lowercase() }
          return@remember (result + unselected).toMutableStateList()
        }

    for (cat in showCategories) {
      // If this category is selected
      val isSelected =
          remember(
              cat,
              selectedCategories,
          ) {
            selectedCategories.firstOrNull { it == cat.id } != null
          }

      DropdownMenuItem(
          enabled = isClickEnabled,
          onClick = {
            if (isSelected) {
              onCategoryRemoved?.invoke(cat)
            } else {
              onCategoryAdded?.invoke(cat)
            }
          },
          text = {
            CategoryChip(
                category = cat,
                isSelected = isSelected,
            )
          },
      )
    }
  }
}
