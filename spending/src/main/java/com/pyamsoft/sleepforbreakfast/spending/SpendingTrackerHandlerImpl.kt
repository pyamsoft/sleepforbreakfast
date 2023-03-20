package com.pyamsoft.sleepforbreakfast.spending

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
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
import com.pyamsoft.sleepforbreakfast.worker.WorkerQueue
import com.pyamsoft.sleepforbreakfast.worker.job.AutomaticSpendingConverterJob
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
    val job = AutomaticSpendingConverterJob

    Timber.d("Enqueue job for processing $automatic: $job")
    workerQueue.enqueue(job)
  }

  override suspend fun processNotification(
      sbn: StatusBarNotification,
      extras: Bundle,
  ) {
    val text = extras.getCharSequence(NotificationCompat.EXTRA_TEXT, "")
    val bigText = extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT, "")

    val payText: CharSequence =
        if (GOOGLE_WALLET_REGEX.containsMatchIn(text)) {
          text
        } else if (GOOGLE_WALLET_REGEX.containsMatchIn(bigText)) {
          bigText
        } else {
          return
        }

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

    val justPrice =
        REGEX_DOLLAR_PRICE.find(payText)
            ?.value
            ?.trim()
            ?.replace(REGEX_FILTER_ONLY_DIGITS, "")
            ?.toLongOrNull()

    Timber.d(
        "PAY received: ${mapOf(
            "match" to payText,
            "price" to justPrice,
            "name" to name,
            "id" to sbn.id,
            "key" to sbn.key,
            "group" to sbn.groupKey,
            "packageName" to sbn.packageName,
            "postTime" to sbn.postTime,
          )}")

    if (justPrice == null) {
      Timber.w("Unable to get justPrice from payText: $payText")
      return
    }

    val automatic =
        DbAutomatic.create(clock)
            .notificationId(sbn.id)
            .notificationKey(sbn.key)
            .notificationGroup(sbn.groupKey)
            .notificationPackageName(sbn.packageName)
            .notificationPostTime(sbn.postTime)
            .notificationMatchText(payText.toString())
            .notificationAmountInCents(justPrice)
            .notificationTitle(name.toString())

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

  @CheckResult
  private inline fun <reified T : Parcelable> Bundle.resolveParcelable(key: String): T? {
    val self = this
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
      self.getParcelable(key, T::class.java)
    } else {
      @Suppress("DEPRECATION") self.getParcelable(key)
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
  }
}
