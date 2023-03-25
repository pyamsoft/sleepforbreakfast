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

package com.pyamsoft.sleepforbreakfast.category

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory

@Composable
internal fun CategoryCard(
    modifier: Modifier = Modifier,
    contentModifier: Modifier = Modifier,
    category: DbCategory,
) {
  val note = category.note
  val hasNote = remember(note) { note.isNotBlank() }

  Card(
      modifier = modifier,
      shape = MaterialTheme.shapes.medium,
      elevation = CardDefaults.Elevation,
  ) {
    Column(
        modifier = contentModifier,
    ) {
      Text(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.baseline),
          text = category.name,
          style =
              MaterialTheme.typography.h6.copy(
                  color =
                      MaterialTheme.colors.onSurface.copy(
                          alpha = ContentAlpha.high,
                      ),
              ),
      )

      Text(
          modifier = Modifier.fillMaxWidth(),
          textAlign = TextAlign.End,
          text = category.accountNumber,
          fontWeight = FontWeight.W700,
          fontFamily = FontFamily.Monospace,
          style =
              MaterialTheme.typography.body2.copy(
                  color =
                      MaterialTheme.colors.onSurface.copy(
                          alpha = ContentAlpha.medium,
                      ),
              ),
      )

      if (hasNote) {
        Text(
            modifier = Modifier.padding(top = MaterialTheme.keylines.content),
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
