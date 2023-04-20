package com.pyamsoft.sleepforbreakfast.transactions.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pyamsoft.sleepforbreakfast.money.list.Search
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewState

@Composable
internal fun HeaderKnobs(
    modifier: Modifier,
    state: TransactionViewState,

    // Search
    onSearchToggle: () -> Unit,

    // Breakdown
    onBreakdownToggle: () -> Unit,

    // Chart
    onChartToggle: () -> Unit,
) {
  Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End,
  ) {
    Search(
        state = state,
        onToggle = onSearchToggle,
    )

    PeriodBreakdown(
        state = state,
        onToggle = onBreakdownToggle,
    )

    SpendingChart(
        state = state,
        onToggle = onChartToggle,
    )
  }
}
