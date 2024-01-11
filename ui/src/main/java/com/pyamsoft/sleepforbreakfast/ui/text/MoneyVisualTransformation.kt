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

package com.pyamsoft.sleepforbreakfast.ui.text

import androidx.annotation.CheckResult
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.DecimalFormat

private val moneyFormatter by lazy {
  DecimalFormat.getCurrencyInstance().apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
  }
}

class MoneyVisualTransformation : VisualTransformation {

  override fun filter(text: AnnotatedString): TransformedText {
    val original = text.text

    // Either we can make this a number or its probably blank
    val money = original.toDoubleOrNull() ?: return VisualTransformation.None.filter(text)

    // Convert cents to dollars
    val moneyText = format(money)

    val originalToTransformed = mutableListOf<Int>()
    val transformedToOriginal = mutableListOf<Int>()

    // We create a map of characters
    var ignoredCharactersCount = 0
    moneyText.forEachIndexed { index, c ->
      // Skip special charcters
      if (c == '$' || c == ' ' || c == ',') {
        // this is a character which exists in the formatted but not the actual value
        ++ignoredCharactersCount
      } else {
        /// We found this character in the index
        originalToTransformed.add(index)
      }

      // constrain to 0 when out of bounds lower
      transformedToOriginal.add(maxOf(index - ignoredCharactersCount, 0))
    }

    return TransformedText(
        AnnotatedString(moneyText),
        object : OffsetMapping {
          override fun originalToTransformed(offset: Int): Int {
            // If we don't have the index, no offset
            return originalToTransformed.getOrElse(offset) { 0 }
          }

          override fun transformedToOriginal(offset: Int): Int {
            // If we don't have the index, no offset
            return transformedToOriginal.getOrElse(offset) { 0 }
          }
        },
    )
  }

  companion object {

    @JvmStatic
    @CheckResult
    private fun format(amount: Double): String {
      return moneyFormatter.format(amount / 100)
    }

    @JvmStatic
    @CheckResult
    fun format(amount: Long): String {
      return format(amount.toDouble())
    }
  }
}
