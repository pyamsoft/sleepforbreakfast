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

package com.pyamsoft.sleepforbreakfast.category

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.TypographyDefaults
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory

@Composable
private fun Tag(
    modifier: Modifier = Modifier,
    text: String,
) {
  Text(
      modifier =
          modifier
              .background(
                  color = MaterialTheme.colorScheme.secondary,
                  shape = MaterialTheme.shapes.small,
              )
              .padding(horizontal = MaterialTheme.keylines.baseline)
              .padding(vertical = MaterialTheme.keylines.typography),
      text = text,
      style =
          MaterialTheme.typography.bodySmall.copy(
              color = MaterialTheme.colorScheme.onSecondary,
          ),
  )
}

@Composable
internal fun CategoryCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    category: DbCategory,
) {
  val note = category.note
  val hasNote = remember(note) { note.isNotBlank() }

  val defaultColor = MaterialTheme.colorScheme.primary
  val color =
      remember(
          defaultColor,
          category,
      ) {
        val c = category.color
        if (c == 0L) defaultColor else Color(c.toULong())
      }

  Card(
      modifier = modifier,
      shape = MaterialTheme.shapes.large,
      elevation = CardDefaults.elevatedCardElevation(),
      colors = CardDefaults.elevatedCardColors(),
  ) {
    Column(
        modifier = contentModifier.padding(MaterialTheme.keylines.content),
    ) {
      Text(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
          text = category.name,
          style =
              MaterialTheme.typography.headlineSmall.copy(
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
              ),
      )

      CategoryColor(
          modifier = Modifier.size(48.dp),
          color = color,
      )

      if (hasNote) {
        Text(
            text = note,
            style =
                MaterialTheme.typography.bodyMedium.copy(
                    color =
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(
                            alpha = TypographyDefaults.ALPHA_DISABLED,
                        ),
                ),
        )
      }
    }
  }
}
