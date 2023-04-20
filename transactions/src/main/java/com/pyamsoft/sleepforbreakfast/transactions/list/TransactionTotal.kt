package com.pyamsoft.sleepforbreakfast.transactions.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.sleepforbreakfast.money.list.SearchBar
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewState
import com.pyamsoft.sleepforbreakfast.transactions.calculateTotalTransactionAmount
import com.pyamsoft.sleepforbreakfast.transactions.calculateTotalTransactionDirection
import com.pyamsoft.sleepforbreakfast.transactions.calculateTotalTransactionRange
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
import java.time.Clock
import kotlin.math.abs

@Composable
internal fun TransactionTotal(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    clock: Clock,

    onDismiss: () -> Unit,

    // Search
    onSearchToggle: () -> Unit,
    onSearchChange: (String) -> Unit,

    // Breakdown
    onBreakdownToggle: () -> Unit,
    onBreakdownChange: (BreakdownRange) -> Unit,

    // Chart
    onChartToggle: () -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    Column(
        modifier = Modifier.background(color = MaterialTheme.colors.primary),
    ) {
      Spacer(
          modifier = Modifier.statusBarsPadding(),
      )

      Totals(
          state = state,
          onDismiss = onDismiss,
          onSearchToggle = onSearchToggle,
          onBreakdownToggle = onBreakdownToggle,
          onChartToggle = onChartToggle,
      )
    }

    SearchBar(
        state = state,
        onToggle = onSearchToggle,
        onChange = onSearchChange,
    )

    PeriodBreakdownBar(
        state = state,
        onToggle = onBreakdownToggle,
        onChange = onBreakdownChange,
    )

    SpendingChartBar(
        state = state,
        clock = clock,
        onToggle = onChartToggle,
    )
  }
}

@Composable
private fun Totals(
    modifier: Modifier = Modifier,
    state: TransactionViewState,

    onDismiss: () -> Unit,

    // Search
    onSearchToggle: () -> Unit,

    // Breakdown
    onBreakdownToggle: () -> Unit,

    // Chart
    onChartToggle: () -> Unit,
) {
  val transactions = state.items.collectAsStateList()

  val totalAmount = remember(transactions) { transactions.calculateTotalTransactionAmount() }
  val totalDirection = remember(totalAmount) { totalAmount.calculateTotalTransactionDirection() }
  val totalRangeNote = remember(transactions) { transactions.calculateTotalTransactionRange() }
  val totalPrice =
      remember(
          transactions,
          totalAmount,
      ) {
        if (transactions.isEmpty()) "$0.00" else MoneyVisualTransformation.format(abs(totalAmount))
      }

  TransactionCard(
      modifier = modifier,
      contentModifier =
          Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.typography),
      priceModifier = Modifier.padding(end = MaterialTheme.keylines.content),
      noteModifier = Modifier.padding(MaterialTheme.keylines.content),
      color = Color.Unspecified,
      shape = RectangleShape,
      elevation = ZeroElevation,
      title = "Total",
      titleStyle = MaterialTheme.typography.h6,
      date = "",
      dateStyle = MaterialTheme.typography.caption,
      price = totalPrice,
      priceDirection = totalDirection,
      priceStyle = MaterialTheme.typography.h4,
      note = totalRangeNote,
      noteStyle = MaterialTheme.typography.body2,
      navigationIcon = {
        IconButton(
            modifier = Modifier.padding(end = MaterialTheme.keylines.content),
            onClick = onDismiss,
        ) {
          Icon(
              imageVector = Icons.Filled.ArrowBack,
              contentDescription = "Back",
              tint = MaterialTheme.colors.onPrimary,
          )
        }
      },
      actions = {
        HeaderKnobs(
            modifier = Modifier.weight(1F),
            state = state,
            onSearchToggle = onSearchToggle,
            onBreakdownToggle = onBreakdownToggle,
            onChartToggle = onChartToggle,
        )
      },
  )
}
