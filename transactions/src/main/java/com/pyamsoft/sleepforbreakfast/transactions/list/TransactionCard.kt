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

package com.pyamsoft.sleepforbreakfast.transactions.list

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.pydroid.ui.util.rememberAsStateList
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.SpendDirection
import com.pyamsoft.sleepforbreakfast.db.transaction.asDirection
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryColor
import com.pyamsoft.sleepforbreakfast.money.add.AddCategories
import com.pyamsoft.sleepforbreakfast.money.category.CategoryIdMapper
import com.pyamsoft.sleepforbreakfast.transactions.TRANSACTION_FORMATTER
import com.pyamsoft.sleepforbreakfast.ui.COLOR_EARN
import com.pyamsoft.sleepforbreakfast.ui.COLOR_SPEND
import com.pyamsoft.sleepforbreakfast.ui.rememberCurrentLocale
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
import java.time.Month
import java.time.format.TextStyle as MonthTextStyle

@Composable
internal fun TransactionHeader(
    modifier: Modifier,
    month: Month,
) {
  val locale = rememberCurrentLocale()

  val monthName =
      remember(
          month,
          locale,
      ) {
        month.getDisplayName(MonthTextStyle.FULL, locale)
      }

  Surface(
      modifier = modifier,
      color = LocalCategoryColor.current,
      contentColor = MaterialTheme.colors.onPrimary,
      shape = MaterialTheme.shapes.medium,
  ) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
        text = "Transactions in $monthName",
        style = MaterialTheme.typography.body1,
        fontWeight = FontWeight.W700,
    )
  }
}

@Composable
internal fun TransactionCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    mapper: CategoryIdMapper,
    transaction: DbTransaction,
    currentCategory: DbCategory.Id,
) {
  val date = transaction.date
  val dateString = remember(date) { TRANSACTION_FORMATTER.get().requireNotNull().format(date) }

  val amount = transaction.amountInCents
  val priceString =
      remember(amount) { if (amount <= 0) "$0.00" else MoneyVisualTransformation.format(amount) }

  val type = transaction.type
  val spendDirection = remember(type) { type.asDirection() }

  TransactionCard(
      modifier = modifier,
      contentModifier =
          Modifier.fillMaxWidth().then(contentModifier).padding(MaterialTheme.keylines.content),
      mapper = mapper,
      title = transaction.name,
      titleStyle =
          MaterialTheme.typography.h6.copy(
              color =
                  MaterialTheme.colors.onSurface.copy(
                      alpha = ContentAlpha.high,
                  ),
          ),
      date = dateString,
      dateStyle =
          MaterialTheme.typography.body2.copy(
              color =
                  MaterialTheme.colors.onSurface.copy(
                      alpha = ContentAlpha.medium,
                  ),
          ),
      price = priceString,
      priceDirection = spendDirection,
      priceStyle = MaterialTheme.typography.h5,
      note = transaction.note,
      noteStyle =
          MaterialTheme.typography.caption.copy(
              color =
                  MaterialTheme.colors.onSurface.copy(
                      alpha = ContentAlpha.disabled,
                  ),
          ),
      currentCategory = currentCategory,
      categories = transaction.categories.rememberAsStateList(),
  )
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun TransactionCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    priceModifier: Modifier = Modifier,
    noteModifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.surface,
    shape: Shape = MaterialTheme.shapes.medium,
    elevation: Dp = CardDefaults.Elevation,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    isHeader: Boolean = false,
    mapper: CategoryIdMapper,
    title: String,
    titleStyle: TextStyle,
    date: String,
    dateStyle: TextStyle,
    price: String,
    priceDirection: SpendDirection,
    priceStyle: TextStyle,
    note: String,
    noteStyle: TextStyle,
    currentCategory: DbCategory.Id,
    categories: SnapshotStateList<DbCategory.Id>,
) {
  val hasDate = remember(date) { date.isNotBlank() }
  val hasNote = remember(note) { note.isNotBlank() }

  val defaultColor = MaterialTheme.colors.onPrimary
  val priceColor =
      remember(
          priceDirection,
          defaultColor,
      ) {
        when (priceDirection) {
          SpendDirection.NONE -> defaultColor
          SpendDirection.SPEND -> COLOR_SPEND
          SpendDirection.EARN -> COLOR_EARN
        }
      }

  val pricePrefix =
      remember(priceDirection) {
        when (priceDirection) {
          SpendDirection.NONE -> " "
          SpendDirection.SPEND -> "-"
          SpendDirection.EARN -> "+"
        }
      }

  Card(
      modifier = modifier,
      shape = shape,
      elevation = elevation,
      backgroundColor = color,
  ) {
    Column(
        modifier = contentModifier,
    ) {
      if (hasDate) {
        Text(
            modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.typography),
            text = date,
            style = dateStyle,
        )
      }

      // This is the Name on cards and the title on the header
      // Don't use TopAppBar
      Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.baseline),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        navigationIcon()

        if (isHeader) {
          Text(
              modifier = Modifier.weight(1F).basicMarquee(),
              text = title,
              style = titleStyle,
              maxLines = 1,
          )
        } else {
          Text(
              modifier = Modifier.weight(1F),
              text = title,
              style = titleStyle,
          )
        }

        actions()
      }

      Text(
          modifier = Modifier.fillMaxWidth().then(priceModifier),
          textAlign = TextAlign.End,
          text = "$pricePrefix$price",
          fontWeight = FontWeight.W700,
          fontFamily = FontFamily.Monospace,
          style =
              priceStyle.copy(
                  color =
                      priceColor.copy(
                          alpha = ContentAlpha.high,
                      ),
              ),
      )

      val otherCategories =
          remember(
              categories,
              currentCategory,
          ) {
            categories.filterNot { it == currentCategory }.toMutableStateList()
          }
      if (otherCategories.isNotEmpty()) {
        AddCategories(
            modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
            canAdd = false,
            showLabel = false,
            selectedCategories = otherCategories,
            mapper = mapper,
            onCategoryAdded = null,
            onCategoryRemoved = null,
        )
      }

      if (hasNote) {
        Text(
            modifier = Modifier.padding(top = MaterialTheme.keylines.baseline).then(noteModifier),
            text = note,
            style = noteStyle,
        )
      }
    }
  }
}
