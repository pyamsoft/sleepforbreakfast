package com.pyamsoft.sleepforbreakfast.transactions

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.sleepforbreakfast.money.list.SearchBar
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
import kotlin.math.abs

@Composable
internal fun TransactionTotal(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    onDismiss: () -> Unit,

    // Search
    onSearchToggled: () -> Unit,
    onSearchUpdated: (String) -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    Totals(
        state = state,
        onDismiss = onDismiss,
        onSearchToggled = onSearchToggled,
    )

    SearchBar(
        state = state,
        onSearchToggled = onSearchToggled,
        onSearchUpdated = onSearchUpdated,
    )
  }
}

@Composable
private fun Totals(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    onDismiss: () -> Unit,

    // Search
    onSearchToggled: () -> Unit,
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
      contentModifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
      color = MaterialTheme.colors.primary,
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
            onSearchToggled = onSearchToggled,
        )
      },
  )
}
