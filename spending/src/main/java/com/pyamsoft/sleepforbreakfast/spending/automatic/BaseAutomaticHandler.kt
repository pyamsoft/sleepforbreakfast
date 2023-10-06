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

package com.pyamsoft.sleepforbreakfast.spending.automatic

import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import com.pyamsoft.sleepforbreakfast.core.REGEX_DOLLAR_PRICE
import com.pyamsoft.sleepforbreakfast.core.REGEX_FILTER_ONLY_DIGITS
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.spending.AutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.PaymentNotification

internal abstract class BaseAutomaticHandler protected constructor() : AutomaticHandler {

  final override suspend fun extract(bundle: Bundle): PaymentNotification? {
    val text = bundle.getCharSequence(NotificationCompat.EXTRA_TEXT, "")
    val bigText = bundle.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT, "")
    val title = bundle.getCharSequence(NotificationCompat.EXTRA_TITLE, "")
    val bigTitle = bundle.getCharSequence(NotificationCompat.EXTRA_TITLE_BIG, "")

    val regex = getRegex()
    val payText: CharSequence =
        if (regex.containsMatchIn(text)) {
          text
        } else if (regex.containsMatchIn(bigText)) {
          bigText
        } else {
          Timber.w {
            "Could not match notification: ${mapOf(
                "text" to text,
                "bigText" to bigText,
                "title" to title,
                "bigTitle" to bigTitle,
                "regex" to regex.pattern,
          )}"
          }
          return null
        }

    val justPrice =
        REGEX_DOLLAR_PRICE.find(payText)
            ?.value
            ?.trim()
            ?.replace(REGEX_FILTER_ONLY_DIGITS, "")
            ?.toLongOrNull()

    if (justPrice == null) {
      Timber.w { "Unable to get justPrice from payText: $payText" }
      return null
    }

    var name = getTitle(title, bigTitle, payText)
    if (name.isBlank()) {
      name = DEFAULT_TITLE
    }

    return PaymentNotification(
        title = name.toString(),
        text = payText.toString(),
        amount = justPrice,
        type = getType(),
        categories = getCategories(),
    )
  }

  @CheckResult
  protected open fun getTitle(
      title: CharSequence,
      bigTitle: CharSequence,
      payText: CharSequence,
  ): CharSequence {
    return if (title.isNotBlank()) {
      title
    } else if (bigTitle.isNotBlank()) {
      bigTitle
    } else {
      DEFAULT_TITLE
    }
  }

  @CheckResult protected abstract fun getRegex(): Regex

  @CheckResult protected abstract fun getType(): DbTransaction.Type

  @CheckResult protected abstract suspend fun getCategories(): List<DbCategory.Id>

  companion object {
    private const val DEFAULT_TITLE = "Automatic Spend Transaction"
  }
}
