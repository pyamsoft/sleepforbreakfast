/*
 * Copyright 2025 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.transactions.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.DATE_FORMATTER
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContainerColor
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContentColor
import com.pyamsoft.sleepforbreakfast.money.calculateTotalTransactionAmount
import com.pyamsoft.sleepforbreakfast.money.calculateTotalTransactionDirection
import com.pyamsoft.sleepforbreakfast.money.calculateTotalTransactionRange
import com.pyamsoft.sleepforbreakfast.money.list.KnobBar
import com.pyamsoft.sleepforbreakfast.money.list.SearchBar
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewState
import com.pyamsoft.sleepforbreakfast.ui.DatePickerDialog
import com.pyamsoft.sleepforbreakfast.ui.model.TransactionDateRange
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Composable
internal fun TransactionTotal(
    modifier: Modifier = Modifier,
    clock: Clock,
    state: TransactionViewState,
    range: TransactionDateRange?,
    onDismiss: () -> Unit,

    // Search
    onSearchToggle: () -> Unit,
    onSearchChange: (String) -> Unit,

    // Date Range
    onDateRangeToggle: () -> Unit,
    onDateRangeChange: (LocalDate, LocalDate) -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    Column(
        modifier =
            Modifier.background(
                color = LocalCategoryContainerColor.current,
                shape =
                    MaterialTheme.shapes.large.copy(
                        topStart = ZeroCornerSize,
                        topEnd = ZeroCornerSize,
                    ),
            ),
    ) {
      Spacer(
          modifier = Modifier.statusBarsPadding(),
      )

      Totals(
          state = state,
          range = range,
          onDismiss = onDismiss,
          onSearchToggle = onSearchToggle,
          onDateRangeToggle = onDateRangeToggle,
      )
    }

    SearchBar(
        state = state,
        onToggle = onSearchToggle,
        onChange = onSearchChange,
    )

    DateRangeBar(
        clock = clock,
        state = state,
        onToggle = onDateRangeToggle,
        onChange = onDateRangeChange,
    )
  }
}

@Composable
private fun Totals(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    range: TransactionDateRange?,
    onDismiss: () -> Unit,
    onSearchToggle: () -> Unit,
    onDateRangeToggle: () -> Unit,
) {
  val contentColor = LocalCategoryContentColor.current
  val category by state.category.collectAsStateWithLifecycle()
  val transactions = state.items.collectAsStateListWithLifecycle()

  val totalAmount = remember(transactions) { transactions.calculateTotalTransactionAmount() }
  val totalDirection = remember(totalAmount) { totalAmount.calculateTotalTransactionDirection() }
  val totalRangeNote =
      remember(
          transactions,
          range,
      ) {
        if (range == null) {
          return@remember transactions.calculateTotalTransactionRange()
        }

        if (range.from == range.to) {
          val dateString = DATE_FORMATTER.format(range.from)
          return@remember "On $dateString"
        }

        val firstDateString = DATE_FORMATTER.format(range.from)
        val lastDateString = DATE_FORMATTER.format(range.to)
        return@remember "From $firstDateString to $lastDateString"
      }

  val totalPrice =
      remember(totalAmount) {
        val t = abs(totalAmount)
        return@remember if (t == 0L) "$0.00" else MoneyVisualTransformation.format(t)
      }

  val title =
      remember(
          category,
      ) {
        val c = category
        if (c == null || c.id.isEmpty) "Total" else c.name
      }

  TransactionCard(
      modifier = modifier,
      contentModifier =
          Modifier.fillMaxWidth()
              .padding(horizontal = MaterialTheme.keylines.typography)
              .padding(bottom = MaterialTheme.keylines.baseline),
      priceModifier = Modifier.padding(end = MaterialTheme.keylines.content),
      noteModifier = Modifier.padding(MaterialTheme.keylines.content),
      shape = RectangleShape,
      colors =
          CardDefaults.cardColors(
              containerColor = Color.Transparent,
              contentColor = contentColor,
          ),
      isHeader = true,
      title = title,
      titleStyle =
          MaterialTheme.typography.headlineSmall.copy(
              color = contentColor,
          ),
      date = "",
      dateStyle =
          MaterialTheme.typography.bodySmall.copy(
              color = contentColor,
          ),
      price = totalPrice,
      priceDirection = totalDirection,
      priceStyle =
          MaterialTheme.typography.headlineLarge.copy(
              color = contentColor,
          ),
      note = totalRangeNote,
      noteStyle =
          MaterialTheme.typography.bodyMedium.copy(
              color = contentColor,
          ),
      navigationIcon = {
        IconButton(
            modifier = Modifier.padding(end = MaterialTheme.keylines.content),
            onClick = onDismiss,
        ) {
          Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = contentColor,
          )
        }
      },
      actions = {
        HeaderKnobs(
            modifier = Modifier.weight(1F),
            state = state,
            onSearchToggle = onSearchToggle,
            onDateRangeToggle = onDateRangeToggle,
        )
      },
      currentCategory = DbCategory.Id.EMPTY,
      categories = remember { mutableStateListOf() },
  )
}

@Composable
fun DateRangeBar(
    modifier: Modifier = Modifier,
    clock: Clock,
    state: TransactionViewState,
    onToggle: () -> Unit,
    onChange: (LocalDate, LocalDate) -> Unit,
) {
  val initialDateRange by state.dateRange.collectAsStateWithLifecycle()
  val isOpen by state.isDateRangeOpen.collectAsStateWithLifecycle()

  // Upon initial open, we save the date range because modification doesn't take effect until
  // both are valid
  val (startDate, setStartDate) = remember { mutableStateOf(initialDateRange?.from) }
  val (endDate, setEndDate) = remember { mutableStateOf(initialDateRange?.to) }

  val (showStartDatePicker, setShowStartDatePicker) = remember { mutableStateOf(false) }
  val (showEndDatePicker, setShowEndDatePicker) = remember { mutableStateOf(false) }

  val handleValidDateRangeSelected by rememberUpdatedState { start: LocalDate?, end: LocalDate? ->
    if (start != null && end != null) {
      onChange(start, end)
    }
  }

  KnobBar(
      modifier = modifier,
      isOpen = isOpen,
      onToggle = onToggle,
  ) {
    RangeDatePicker(
        show = showStartDatePicker,
        clock = clock,
        date = startDate,
        fallbackText = "Set Start Date",
        onSetShow = { setShowStartDatePicker(it) },
        onDatePicked = { d ->
          setStartDate(d)
          handleValidDateRangeSelected(d, endDate)
        },
    )

    RangeDatePicker(
        modifier = Modifier.padding(start = MaterialTheme.keylines.content),
        show = showEndDatePicker,
        clock = clock,
        date = endDate,
        fallbackText = "Set End Date",
        onSetShow = { setShowEndDatePicker(it) },
        onDatePicked = { d ->
          setEndDate(d)
          handleValidDateRangeSelected(startDate, d)
        },
    )
  }
}

@Composable
private fun RangeDatePicker(
    modifier: Modifier = Modifier,
    show: Boolean,
    clock: Clock,
    date: LocalDate?,
    fallbackText: String,
    onSetShow: (Boolean) -> Unit,
    onDatePicked: (LocalDate) -> Unit,
) {
  TextButton(
      modifier = modifier,
      onClick = { onSetShow(true) },
  ) {
    Text(
        text = date?.format(DateTimeFormatter.ISO_DATE) ?: fallbackText,
    )
  }

  if (show) {
    DatePickerDialog(
        initialDate = remember { date ?: LocalDate.now(clock) },
        onDismiss = { onSetShow(false) },
        onDateSelected = { onDatePicked(it) },
    )
  }
}
