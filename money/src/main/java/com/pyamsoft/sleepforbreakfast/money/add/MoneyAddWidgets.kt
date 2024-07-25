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

package com.pyamsoft.sleepforbreakfast.money.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
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
import java.time.LocalDate
import java.time.LocalTime

// This is basically longer than any expected money amount.
private const val MAX_ALLOWED_AMOUNT_LENGTH = 18

@Composable
fun MoneyAmount(
    modifier: Modifier = Modifier,
    state: MoneyAddViewState,
    keyboardNumberOptions: KeyboardOptions,
    onAmountChanged: (String) -> Unit,
) {
  val amount by state.amount.collectAsStateWithLifecycle()

  Row(
      modifier = modifier,
      verticalAlignment = Alignment.CenterVertically,
  ) {
    TextField(
        modifier = Modifier.weight(1F),
        value = amount,
        onValueChange = { v ->
          // No long strings
          if (v.length >= MAX_ALLOWED_AMOUNT_LENGTH) {
            return@TextField
          }

          // No white space
          if (v.contains(Regex("\\s"))) {
            return@TextField
          }

          // Also don't allow other characters
          if (v.contains(Regex("[,\\-/_]"))) {
            return@TextField
          }

          // Can't start with decimal
          if (v.startsWith('.')) {
            return@TextField
          }

          // No double decimals
          if (v.count { it == '.' } >= 2) {
            return@TextField
          }

          onAmountChanged(v)
        },
        keyboardOptions = keyboardNumberOptions,
        leadingIcon = {
          Icon(
              imageVector = Icons.Filled.AttachMoney,
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
    onNameChanged: (String) -> Unit,
) {
  val name by state.name.collectAsStateWithLifecycle()

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
  val note by state.note.collectAsStateWithLifecycle()
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
      colors = TextFieldDefaults.colors())
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
@OptIn(ExperimentalLayoutApi::class)
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
            imageVector = Icons.Filled.Add,
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
        val h =
            configuration.run {
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
