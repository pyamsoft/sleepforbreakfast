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

package com.pyamsoft.sleepforbreakfast.transactions.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.ZeroCornerSize
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.DATE_FORMATTER
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryColor
import com.pyamsoft.sleepforbreakfast.money.calculateTotalTransactionAmount
import com.pyamsoft.sleepforbreakfast.money.calculateTotalTransactionDirection
import com.pyamsoft.sleepforbreakfast.money.calculateTotalTransactionRange
import com.pyamsoft.sleepforbreakfast.money.list.SearchBar
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewState
import com.pyamsoft.sleepforbreakfast.ui.model.TransactionDateRange
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
import kotlin.math.abs

@Composable
internal fun TransactionTotal(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    range: TransactionDateRange?,
    onDismiss: () -> Unit,
    onSearchToggle: () -> Unit,
    onSearchChange: (String) -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    Column(
        modifier =
            Modifier.background(
                color = LocalCategoryColor.current,
                shape =
                    MaterialTheme.shapes.medium.copy(
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
      )
    }

    SearchBar(
        state = state,
        onToggle = onSearchToggle,
        onChange = onSearchChange,
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
) {
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
      color = Color.Unspecified,
      shape = RectangleShape,
      elevation = ZeroElevation,
      isHeader = true,
      title = title,
      titleStyle =
          MaterialTheme.typography.h6.copy(
              color = MaterialTheme.colors.onPrimary,
          ),
      date = "",
      dateStyle =
          MaterialTheme.typography.caption.copy(
              color = MaterialTheme.colors.onPrimary,
          ),
      price = totalPrice,
      priceDirection = totalDirection,
      priceStyle =
          MaterialTheme.typography.h4.copy(
              color = MaterialTheme.colors.onPrimary,
          ),
      note = totalRangeNote,
      noteStyle =
          MaterialTheme.typography.body2.copy(
              color = MaterialTheme.colors.onPrimary,
          ),
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
        )
      },
      currentCategory = DbCategory.Id.EMPTY,
      categories = remember { mutableStateListOf() },
  )
}
