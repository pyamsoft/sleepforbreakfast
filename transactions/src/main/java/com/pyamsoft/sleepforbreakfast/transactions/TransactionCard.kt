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

package com.pyamsoft.sleepforbreakfast.transactions

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
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.SpendDirection
import com.pyamsoft.sleepforbreakfast.db.transaction.asDirection
import com.pyamsoft.sleepforbreakfast.ui.rememberCurrentLocale
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
import java.time.Month
import java.time.format.TextStyle as MonthTextStyle

@Composable
internal fun TransactionHeader(modifier: Modifier, month: Month) {
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
      color = MaterialTheme.colors.primary,
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
    transaction: DbTransaction,
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
      title = transaction.name,
      titleStyle = MaterialTheme.typography.h6,
      date = dateString,
      dateStyle = MaterialTheme.typography.caption,
      price = priceString,
      priceDirection = spendDirection,
      priceStyle = MaterialTheme.typography.h5,
      note = transaction.note,
      noteStyle = MaterialTheme.typography.body2,
  )
}

@Composable
internal fun TransactionCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.surface,
    shape: Shape = MaterialTheme.shapes.medium,
    elevation: Dp = CardDefaults.Elevation,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    title: String,
    titleStyle: TextStyle,
    date: String,
    dateStyle: TextStyle,
    price: String,
    priceDirection: SpendDirection,
    priceStyle: TextStyle,
    note: String,
    noteStyle: TextStyle,
) {
  val hasDate = remember(date) { date.isNotBlank() }
  val hasNote = remember(note) { note.isNotBlank() }

  val defaultColor = MaterialTheme.colors.onSurface
  val priceColor =
      remember(
          priceDirection,
          defaultColor,
      ) {
        when (priceDirection) {
          SpendDirection.NONE -> defaultColor
          SpendDirection.LOSS -> Color.Red
          SpendDirection.GAIN -> Color.Green
        }
      }

  val pricePrefix =
      remember(priceDirection) {
        when (priceDirection) {
          SpendDirection.NONE -> " "
          SpendDirection.LOSS -> "-"
          SpendDirection.GAIN -> "+"
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
            style =
                dateStyle.copy(
                    color =
                        MaterialTheme.colors.onSurface.copy(
                            alpha = ContentAlpha.disabled,
                        ),
                ),
        )
      }

      // This is the Name on cards and the title on the header
      // Don't use TopAppBar
      Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.baseline),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        navigationIcon()

        Text(
            text = title,
            style =
                titleStyle.copy(
                    color =
                        MaterialTheme.colors.onSurface.copy(
                            alpha = ContentAlpha.medium,
                        ),
                ),
        )

        actions()
      }

      Text(
          modifier = Modifier.fillMaxWidth(),
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

      if (hasNote) {
        Text(
            modifier = Modifier.padding(top = MaterialTheme.keylines.content),
            text = note,
            style =
                noteStyle.copy(
                    color =
                        MaterialTheme.colors.onSurface.copy(
                            alpha = ContentAlpha.medium,
                        ),
                ),
        )
      }
    }
  }
}
