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
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.theme.success
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.pydroid.ui.util.isPortrait
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.DATE_FORMATTER
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryColor
import com.pyamsoft.sleepforbreakfast.money.TIME_FORMATTER
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
  val spendColor = MaterialTheme.colors.error
  val earnColor = MaterialTheme.colors.success
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
              color = color.copy(alpha = ContentAlpha.medium),
              shape = shape,
          ),
      elevation = CardDefaults.Elevation,
      backgroundColor = if (isSelected) color else MaterialTheme.colors.surface,
      contentColor = if (isSelected) MaterialTheme.colors.onSecondary else color,
      shape = shape,
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
          style = MaterialTheme.typography.body2,
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
  val justDate = remember(date) { DATE_FORMATTER.get().requireNotNull().format(date) }

  Text(
      modifier = modifier.clickable { onOpenDateDialog() },
      text = justDate,
      style = MaterialTheme.typography.h6,
      textAlign = TextAlign.Center,
  )
}

@Composable
fun TimePicker(
    modifier: Modifier = Modifier,
    time: LocalTime,
    onOpenTimeDialog: () -> Unit,
) {
  val justTime = remember(time) { TIME_FORMATTER.get().requireNotNull().format(time) }

  Text(
      modifier = modifier.clickable { onOpenTimeDialog() },
      text = justTime,
      style = MaterialTheme.typography.h6,
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
                contentColor = LocalCategoryColor.current,
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
                backgroundColor = LocalCategoryColor.current,
                contentColor = MaterialTheme.colors.onPrimary,
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
  )
}

@Composable
private fun CategoryChip(
    modifier: Modifier = Modifier,
    category: DbCategory,
    color: Color?,
) {
  val backgroundColor = remember(color) { color ?: Color.Unspecified }
  val unselectedTextColor = MaterialTheme.colors.onSurface
  val selectedTextColor = MaterialTheme.colors.onSecondary
  val textColor =
      remember(
          color,
          unselectedTextColor,
          selectedTextColor,
      ) {
        if (color == null) unselectedTextColor else selectedTextColor
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
          MaterialTheme.typography.caption.copy(
              color =
                  textColor.copy(
                      alpha = ContentAlpha.high,
                  ),
          ),
  )
}

@Composable
fun MoneyCategories(
    modifier: Modifier = Modifier,
    state: MoneyAddViewState,
    canAdd: Boolean,
    showLabel: Boolean,
    allCategories: SnapshotStateList<DbCategory>,
    onCategoryAdded: ((DbCategory) -> Unit)?,
    onCategoryRemoved: ((DbCategory) -> Unit)?,
) {
  val categories = state.categories.collectAsStateListWithLifecycle()
  AddCategories(
      modifier = modifier,
      canAdd = canAdd,
      showLabel = showLabel,
      selectedCategories = categories,
      allCategories = allCategories,
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
    selectedCategories: SnapshotStateList<DbCategory.Id>,
    allCategories: SnapshotStateList<DbCategory>,
    onCategoryAdded: ((DbCategory) -> Unit)?,
    onCategoryRemoved: ((DbCategory) -> Unit)?,
) {
  val defaultColor = MaterialTheme.colors.secondary

  // TODO move into VM
  val (show, setShow) = rememberSaveable { mutableStateOf(false) }

  val handleShow by rememberUpdatedState { setShow(true) }

  Column(
      modifier = modifier,
  ) {
    if (showLabel) {
      Text(
          modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
          text = "Categories",
          fontWeight = FontWeight.W700,
          color =
              MaterialTheme.colors.onSurface.copy(
                  alpha = ContentAlpha.disabled,
              ),
          style = MaterialTheme.typography.caption,
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

      val showCategories =
          remember(selectedCategories, allCategories) {
            val result = mutableStateListOf<DbCategory>()
            for (id in selectedCategories) {
              // If a category is delete but still "attached" to the Transaction or Repeat, it will
              // be
              // null-ish here as it won't be in the allCategories, so hide it
              val maybeCategory = allCategories.firstOrNull { it.id == id }
              if (maybeCategory != null) {
                result.add(maybeCategory)
              }
            }

            return@remember result.sortedBy { it.name.lowercase() }
          }

      for (cat in showCategories) {
        val color =
            remember(
                cat,
                defaultColor,
            ) {
              val c = cat.color
              if (c == 0L) defaultColor else Color(c.toULong())
            }

        CategoryChip(
            modifier =
                Modifier.padding(end = MaterialTheme.keylines.baseline)
                    .padding(bottom = MaterialTheme.keylines.baseline)
                    .clickable(enabled = canAdd) { handleShow() },
            color = color,
            category = cat,
        )
      }
    }
  }

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
      modifier = Modifier.heightIn(max = dropdownMaxHeight),
      properties = remember { PopupProperties(focusable = true) },
      expanded = show,
      onDismissRequest = { setShow(false) },
  ) {

    // Remember this only on the initial composition to keep the "starting selected" on top,
    // then alphabetical. New selections will not go to the top
    val showCategories = remember {
      val result = mutableStateListOf<DbCategory>()
      val unselected = mutableStateListOf<DbCategory>()
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
      return@remember result + unselected
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

      val color =
          remember(
              cat,
              defaultColor,
          ) {
            val c = cat.color
            if (c == 0L) defaultColor else Color(c.toULong())
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
      ) {
        CategoryChip(
            category = cat,
            color = if (isSelected) color else null,
        )
      }
    }
  }
}
