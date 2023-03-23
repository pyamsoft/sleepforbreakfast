package com.pyamsoft.sleepforbreakfast.spending

import android.os.Bundle
import android.service.notification.StatusBarNotification
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.core.REGEX_DOLLAR_PRICE
import com.pyamsoft.sleepforbreakfast.core.REGEX_FILTER_ONLY_DIGITS
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.automatic.queryByAutomaticNotification
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.worker.WorkJobType
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
internal class SpendingTrackerHandlerImpl
@Inject
internal constructor(
    private val automaticQueryDao: AutomaticQueryDao,
    private val automaticInsertDao: AutomaticInsertDao,
    private val workerQueue: WorkerQueue,
    private val clock: Clock,
) : SpendingTrackerHandler {

  private suspend fun handleProcessUnusedAutomatic(automatic: DbAutomatic) {
    val job = WorkJobType.ONESHOT_AUTOMATIC_TRANSACTION
    Timber.d("Enqueue job for processing $automatic: $job")
    workerQueue.enqueue(job)
  }

  private data class PaymentNotification(
      val text: CharSequence,
      val amount: Long,
      val type: DbTransaction.Type,
  )

  @CheckResult
  private fun extractGoogleWalletNotification(
      text: CharSequence,
      bigText: CharSequence,
  ): PaymentNotification? {
    val payText: CharSequence =
        if (GOOGLE_WALLET_REGEX.containsMatchIn(text)) {
          text
        } else if (GOOGLE_WALLET_REGEX.containsMatchIn(bigText)) {
          bigText
        } else {
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

    return PaymentNotification(
        text = payText,
        amount = justPrice,
        type = DbTransaction.Type.SPEND,
    )
  }

  @CheckResult
  private fun extractVenmoNotification(
      text: CharSequence,
      bigText: CharSequence,
  ): PaymentNotification? {
    val payText: CharSequence =
        if (VENMO_WALLET_REGEX.containsMatchIn(text)) {
          text
        } else if (VENMO_WALLET_REGEX.containsMatchIn(bigText)) {
          bigText
        } else {
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

    return PaymentNotification(
        text = payText,
        amount = justPrice,
        type = DbTransaction.Type.SPEND,
    )
  }

  @CheckResult
  private fun extractAutomaticPayment(
      text: CharSequence,
      bigText: CharSequence
  ): PaymentNotification? {
    var result = extractGoogleWalletNotification(text, bigText)
    if (result != null) {
      return result
    }

    result = extractVenmoNotification(text, bigText)
    if (result != null) {
      return result
    }

    return null
  }

  override suspend fun processNotification(
      sbn: StatusBarNotification,
      extras: Bundle,
  ) {
    val text = extras.getCharSequence(NotificationCompat.EXTRA_TEXT, "")
    val bigText = extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT, "")

    val automaticPayment = extractAutomaticPayment(text, bigText) ?: return

    val title = extras.getCharSequence(NotificationCompat.EXTRA_TITLE, "")
    val bigTitle = extras.getCharSequence(NotificationCompat.EXTRA_TITLE_BIG, "")

    val name =
        if (title.isNotBlank()) {
          title
        } else if (bigTitle.isNotBlank()) {
          bigTitle
        } else {
          "Automatic Spend Transaction"
        }

    val automatic =
        DbAutomatic.create(clock)
            .notificationId(sbn.id)
            .notificationKey(sbn.key)
            .notificationGroup(sbn.groupKey)
            .notificationPackageName(sbn.packageName)
            .notificationPostTime(sbn.postTime)
            .notificationTitle(name.toString())
            .notificationMatchText(automaticPayment.text.toString())
            .notificationAmountInCents(automaticPayment.amount)
            .notificationType(automaticPayment.type)

    when (val existing = automaticQueryDao.queryByAutomaticNotification(automatic)) {
      is Maybe.Data -> {
        Timber.w(
            "Found existing automatic notification matching parameters: ${mapOf(
                  "NEW" to automatic,
                  "EXISTING" to existing,
              )}")
      }
      is Maybe.None -> {
        when (val result = automaticInsertDao.insert(automatic)) {
          is DbInsert.InsertResult.Fail -> {
            Timber.e(result.error, "Failed to insert automatic $automatic")
          }
          is DbInsert.InsertResult.Update -> {
            Timber.d("Update existing automatic: $automatic")
          }
          is DbInsert.InsertResult.Insert -> {
            Timber.d("Inserted automatic: $automatic")
            handleProcessUnusedAutomatic(automatic)
          }
        }
      }
    }
  }

  companion object {

    /**
     * Google Wallet posts messages like
     *
     * $123.45 with Amex •••• 1234
     *
     * We can look for that notification text and parse the values out
     */
    private val GOOGLE_WALLET_REGEX = "\\$.* with .* ••••".toRegex(RegexOption.MULTILINE)

    /**
     * Venmo posts messages like
     *
     * You completed Tom Smith's request for $123.45 - Note about payment here
     *
     * We can look for that notification text and parse the values out
     */
    private val VENMO_WALLET_REGEX =
        "You completed .* request for \\$.*".toRegex(RegexOption.MULTILINE)
  }
}
