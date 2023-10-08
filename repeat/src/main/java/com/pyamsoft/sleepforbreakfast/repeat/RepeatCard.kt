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

package com.pyamsoft.sleepforbreakfast.repeat

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.pydroid.ui.util.rememberAsStateList
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.transaction.SpendDirection
import com.pyamsoft.sleepforbreakfast.db.transaction.asDirection
import com.pyamsoft.sleepforbreakfast.money.add.AddCategories
import com.pyamsoft.sleepforbreakfast.ui.COLOR_EARN
import com.pyamsoft.sleepforbreakfast.ui.COLOR_SPEND
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val repeatFormatter by lazy { DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG) }

@Composable
internal fun RepeatCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    repeat: DbRepeat,
) {
  val note = repeat.transactionNote
  val hasNote = remember(note) { note.isNotBlank() }
  val transactionType = repeat.transactionType
  val spendDirection = remember(transactionType) { transactionType.asDirection() }

  val amount = repeat.transactionAmountInCents
  val priceString =
      remember(amount) { if (amount <= 0) "$0.00" else MoneyVisualTransformation.format(amount) }

  val defaultColor = MaterialTheme.colors.onSurface
  val priceColor =
      remember(
          spendDirection,
          defaultColor,
      ) {
        when (spendDirection) {
          SpendDirection.NONE -> defaultColor
          SpendDirection.SPEND -> COLOR_SPEND
          SpendDirection.EARN -> COLOR_EARN
        }
      }

  val pricePrefix =
      remember(spendDirection) {
        when (spendDirection) {
          SpendDirection.NONE -> " "
          SpendDirection.SPEND -> "-"
          SpendDirection.EARN -> "+"
        }
      }

  val startDate = repeat.firstDay
  val startDateString = remember(startDate) { repeatFormatter.format(startDate) }

  Card(
      modifier = modifier,
      shape = MaterialTheme.shapes.medium,
      elevation = CardDefaults.Elevation,
  ) {
    Column(
        Modifier.fillMaxWidth().then(contentModifier).padding(MaterialTheme.keylines.content),
    ) {
      Text(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.baseline),
          text = repeat.transactionName,
          style =
              MaterialTheme.typography.h5.copy(
                  color =
                      MaterialTheme.colors.onSurface.copy(
                          alpha = ContentAlpha.high,
                      ),
              ),
      )

      Row(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.baseline),
          verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
            modifier = Modifier.weight(1F),
            text = repeat.repeatType.displayName,
            fontWeight = FontWeight.W700,
            style =
                MaterialTheme.typography.body2.copy(
                    color =
                        MaterialTheme.colors.onSurface.copy(
                            alpha = ContentAlpha.medium,
                        ),
                ),
        )

        Text(
            textAlign = TextAlign.End,
            text = "$pricePrefix$priceString",
            fontWeight = FontWeight.W700,
            fontFamily = FontFamily.Monospace,
            style =
                MaterialTheme.typography.h6.copy(
                    color =
                        priceColor.copy(
                            alpha = ContentAlpha.high,
                        ),
                ),
        )
      }

      Text(
          text = "Starting on $startDateString",
          style =
              MaterialTheme.typography.body2.copy(
                  color =
                      MaterialTheme.colors.onSurface.copy(
                          alpha = ContentAlpha.medium,
                      ),
              ),
      )

      AddCategories(
          canAdd = false,
          showLabel = true,
          selectedCategories = repeat.transactionCategories.rememberAsStateList(),
          onCategoryAdded = null,
          onCategoryRemoved = null,
      )

      if (hasNote) {
        Text(
            modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
            text = note,
            style =
                MaterialTheme.typography.caption.copy(
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
