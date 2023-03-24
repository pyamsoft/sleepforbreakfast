package com.pyamsoft.sleepforbreakfast.spending.automatic

import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import com.pyamsoft.sleepforbreakfast.core.REGEX_DOLLAR_PRICE
import com.pyamsoft.sleepforbreakfast.core.REGEX_FILTER_ONLY_DIGITS
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.spending.AutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.PaymentNotification
import timber.log.Timber

internal abstract class BaseAutomaticHandler protected constructor() : AutomaticHandler {

  final override fun extract(bundle: Bundle): PaymentNotification? {
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
          Timber.w(
              "Could not match notification: ${mapOf(
                "text" to text,
                "bigText" to bigText,
                "title" to title,
                "bigTitle" to bigTitle,
                "regex" to regex.pattern,
          )}")
          return null
        }

    val justPrice =
        REGEX_DOLLAR_PRICE.find(payText)
            ?.value
            ?.trim()
            ?.replace(REGEX_FILTER_ONLY_DIGITS, "")
            ?.toLongOrNull()

    if (justPrice == null) {
      Timber.w("Unable to get justPrice from payText: $payText")
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

  companion object {
    private const val DEFAULT_TITLE = "Automatic Spend Transaction"
  }
}