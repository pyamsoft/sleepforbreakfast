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

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.TypographyDefaults
import com.pyamsoft.pydroid.ui.util.rememberAsStateList
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.SpendDirection
import com.pyamsoft.sleepforbreakfast.db.transaction.asDirection
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContainerColor
import com.pyamsoft.sleepforbreakfast.money.LocalCategoryContentColor
import com.pyamsoft.sleepforbreakfast.money.add.AddCategories
import com.pyamsoft.sleepforbreakfast.ui.COLOR_EARN
import com.pyamsoft.sleepforbreakfast.ui.COLOR_SPEND
import com.pyamsoft.sleepforbreakfast.ui.rememberCurrentLocale
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.format.TextStyle as MonthTextStyle

private val transactionFormatter: DateTimeFormatter by lazy {
  DateTimeFormatter.ofLocalizedDateTime(
          FormatStyle.LONG,
          FormatStyle.SHORT,
      )
      .requireNotNull()
}

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
      color = LocalCategoryContainerColor.current,
      contentColor = LocalCategoryContentColor.current,
      shape = MaterialTheme.shapes.large,
  ) {
    Text(
        modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
        text = "Transactions in $monthName",
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.W700,
    )
  }
}

@Composable
internal fun TransactionCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    transaction: DbTransaction,
    currentCategory: DbCategory.Id,
) {
  val date = transaction.date
  val dateString = remember(date) { transactionFormatter.format(date) }

  val amount = transaction.amountInCents
  val priceString =
      remember(amount) { if (amount <= 0) "$0.00" else MoneyVisualTransformation.format(amount) }

  val type = transaction.type
  val spendDirection = remember(type) { type.asDirection() }

  TransactionCard(
      modifier = modifier,
      contentModifier =
          Modifier.fillMaxWidth().then(contentModifier).padding(MaterialTheme.keylines.content),
      title = transaction.name,
      titleStyle =
          MaterialTheme.typography.headlineSmall.copy(
              color = MaterialTheme.colorScheme.onSurface,
          ),
      date = dateString,
      dateStyle =
          MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurfaceVariant,
          ),
      price = priceString,
      priceDirection = spendDirection,
      priceStyle = MaterialTheme.typography.headlineMedium,
      note = transaction.note,
      noteStyle =
          MaterialTheme.typography.bodySmall.copy(
              color =
                  MaterialTheme.colorScheme.onSurfaceVariant.copy(
                      alpha = TypographyDefaults.ALPHA_DISABLED),
          ),
      currentCategory = currentCategory,
      categories = transaction.categories.rememberAsStateList(),
  )
}

@Composable
internal fun TransactionCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    priceModifier: Modifier = Modifier,
    noteModifier: Modifier = Modifier,
    colors: CardColors = CardDefaults.cardColors(),
    shape: Shape = MaterialTheme.shapes.large,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    isHeader: Boolean = false,
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
    categories: List<DbCategory.Id>,
) {
  val hasDate = remember(date) { date.isNotBlank() }
  val hasNote = remember(note) { note.isNotBlank() }

  val defaultColor = MaterialTheme.colorScheme.onPrimary
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
      colors = colors,
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
                  color = priceColor,
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
