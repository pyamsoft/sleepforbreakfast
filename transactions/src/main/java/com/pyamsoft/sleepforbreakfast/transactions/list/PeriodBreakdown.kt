package com.pyamsoft.sleepforbreakfast.transactions.list

import androidx.annotation.CheckResult
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Warning
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.success
import com.pyamsoft.pydroid.theme.warning
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.money.DATE_FORMATTER
import com.pyamsoft.sleepforbreakfast.money.list.KnobBar
import com.pyamsoft.sleepforbreakfast.money.list.UsageIndicator
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewState
import com.pyamsoft.sleepforbreakfast.ui.DatePickerDialog
import java.time.LocalDate

@Composable
internal fun PeriodBreakdown(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    onToggle: () -> Unit,
) {
  val breakdown by state.breakdown.collectAsState()
  val isOpen by state.isBreakdownOpen.collectAsState()

  val show = remember(breakdown) { breakdown != null }

  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomEnd,
  ) {
    IconButton(
        onClick = onToggle,
    ) {
      Icon(
          imageVector = Icons.Filled.DateRange,
          contentDescription = "Date Range Breakdown",
          tint =
              MaterialTheme.colors.onPrimary.copy(
                  alpha = if (isOpen) ContentAlpha.high else ContentAlpha.medium,
              ),
      )
    }

    UsageIndicator(
        show = show,
    )
  }
}

@CheckResult
private fun resolveCurrentBreakdown(
    range: BreakdownRange?,
    start: LocalDate?,
    end: LocalDate?
): Maybe<out BreakdownRange>? {
  if (start != null && end != null) {
    return if (start < end) {
      Maybe.Data(
          BreakdownRange(
              start = start,
              end = end,
          ),
      )
    } else {
      Maybe.None
    }
  } else if (range != null) {
    if (start != null) {
      return if (start < range.end) {
        Maybe.Data(range.copy(start = start))
      } else {
        Maybe.None
      }
    } else if (end != null) {
      return if (end > range.start) {
        Maybe.Data(range.copy(end = end))
      } else {
        Maybe.None
      }
    }
  }

  return null
}

@Composable
internal fun PeriodBreakdownBar(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    onToggle: () -> Unit,
    onChange: (BreakdownRange) -> Unit,
) {
  val range by state.breakdown.collectAsState()
  val isOpen by state.isBreakdownOpen.collectAsState()

  val (isStartDateOpen, setStartDateOpen) = remember { mutableStateOf(false) }
  val (selectedStart, setSelectedStart) = remember { mutableStateOf<LocalDate?>(null) }
  val (isEndDateOpen, setEndDateOpen) = remember { mutableStateOf(false) }
  val (selectedEnd, setSelectedEnd) = remember { mutableStateOf<LocalDate?>(null) }

  val startDate =
      remember(
          range,
          selectedStart,
      ) {
        if (selectedStart == null) {
          range.let { r ->
            if (r == null) {
              "Start Date"
            } else {
              DATE_FORMATTER.get().requireNotNull().format(r.start)
            }
          }
        } else {
          DATE_FORMATTER.get().requireNotNull().format(selectedStart)
        }
      }

  val endDate =
      remember(
          range,
          selectedEnd,
      ) {
        if (selectedEnd == null) {
          range.let { r ->
            if (r == null) {
              "End Date"
            } else {
              DATE_FORMATTER.get().requireNotNull().format(r.end)
            }
          }
        } else {
          DATE_FORMATTER.get().requireNotNull().format(selectedEnd)
        }
      }

  val currentRange =
      remember(range, selectedStart, selectedEnd) {
        resolveCurrentBreakdown(
            range,
            selectedStart,
            selectedEnd,
        )
      }
  // When a valid range is found by combining selections, push it
  val handleChange by rememberUpdatedState(onChange)
  LaunchedEffect(currentRange) {
    if (currentRange != null && currentRange is Maybe.Data) {
      handleChange(currentRange.data)
    }
  }

  KnobBar(
      modifier = modifier,
      isOpen = isOpen,
      onToggle = onToggle,
  ) {
    Text(
        modifier = Modifier.clickable { setStartDateOpen(true) },
        text = startDate,
    )

    Box(
        modifier = Modifier.weight(1F),
        contentAlignment = Alignment.Center,
    ) {
      if (currentRange != null) {
        Icon(
            imageVector =
                if (currentRange is Maybe.None) Icons.Filled.Warning else Icons.Filled.Check,
            contentDescription = if (currentRange is Maybe.None) "Invalid Range" else "Date Range",
            tint =
                if (currentRange is Maybe.None) MaterialTheme.colors.warning
                else MaterialTheme.colors.success,
        )
      }
    }

    Text(
        modifier = Modifier.clickable { setEndDateOpen(true) },
        text = endDate,
    )
  }

  if (isStartDateOpen) {
    DatePickerDialog(
        // TODO don't use without Clock
        initialDate = range?.start ?: LocalDate.now(),
        onDismiss = { setStartDateOpen(false) },
        onDateSelected = { setSelectedStart(it) },
    )
  }

  if (isEndDateOpen) {
    DatePickerDialog(
        // TODO don't use without Clock
        initialDate = range?.end ?: LocalDate.now(),
        onDismiss = { setEndDateOpen(false) },
        onDateSelected = { setSelectedEnd(it) },
    )
  }
}
