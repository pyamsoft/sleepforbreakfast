package com.pyamsoft.sleepforbreakfast.service

import android.content.pm.ApplicationInfo
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.core.REGEX_DOLLAR_PRICE
import com.pyamsoft.sleepforbreakfast.core.REGEX_FILTER_ONLY_DIGITS
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

private val GOOGLE_WALLET_REGEX = "\\$.* with .* ••••".toRegex(RegexOption.MULTILINE)

class SpendingTrackerService : NotificationListenerService() {
  private var scope: CoroutineScope? = null

  @Inject @JvmField internal var clock: Clock? = null
  @Inject @JvmField internal var transactionInsertDao: TransactionInsertDao? = null

  private fun withScope(block: CoroutineScope.() -> Unit) {
    scope?.also(block)
  }

  override fun onListenerConnected() {
    Timber.d("Service connected!")

    scope?.cancel()
    scope = MainScope()
  }

  override fun onListenerDisconnected() {
    Timber.d("Service disconnected!")

    scope?.cancel()
    scope = null

    clock = null
    transactionInsertDao = null
  }

  override fun onNotificationPosted(sbn: StatusBarNotification) {
    withScope { launch(context = Dispatchers.IO) { processNotification(sbn) } }
  }

  private fun ensureInjected() {
    if (clock == null || transactionInsertDao == null) {
      ObjectGraph.ApplicationScope.retrieve(application).inject(this)
    }
  }

  private suspend fun processNotification(sbn: StatusBarNotification) {
    Enforcer.assertOffMainThread()

    ensureInjected()

    Timber.d(
        "New notification: ${mapOf(
      "id" to sbn.id,
      "packageName" to sbn.packageName,
      "groupKey" to sbn.groupKey,
      "postTime" to sbn.postTime,
      "key" to sbn.key,
    )}")

    val notif = sbn.notification
    if (notif == null) {
      Timber.w("SBN is missing notification data")
      return
    }

    val extras = notif.extras
    if (extras == null) {
      Timber.w("SBN Notification data is missing extras bundle")
      return
    }

    val text = extras.getCharSequence(NotificationCompat.EXTRA_TEXT, "")
    val bigText = extras.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT, "")

    var payText: CharSequence? = null
    if (GOOGLE_WALLET_REGEX.containsMatchIn(text)) {
      payText = text
    } else if (GOOGLE_WALLET_REGEX.containsMatchIn(bigText)) {
      payText = bigText
    }

    payText?.also { pt ->
      val appInfo = extras.resolveParcelable<ApplicationInfo>("android.appInfo")

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
          REGEX_DOLLAR_PRICE.find(pt)
              ?.value
              ?.trim()
              ?.replace(REGEX_FILTER_ONLY_DIGITS, "")
              ?.toLongOrNull()

      val appName: CharSequence? =
          appInfo?.let {
            try {
              application.packageManager.getApplicationLabel(it)
            } catch (e: Throwable) {
              Timber.e(e, "Error loading AppInfo label")
              null
            }
          }

      val appIcon: Drawable? =
          appInfo?.let {
            try {
              application.packageManager.getApplicationIcon(it)
            } catch (e: Throwable) {
              Timber.e(e, "Error loading AppInfo Icon")
              null
            }
          }

      Timber.d(
          "PAY received: ${mapOf(
          "PRICE" to justPrice,
          "NAME" to name,
          "App Info" to appInfo,
          "App Label" to appName,
          "App Icon" to appIcon,
        )}")

      justPrice?.also { p ->
        val transaction =
            DbTransaction.create(
                    clock = clock.requireNotNull(),
                    id = DbTransaction.Id(sbn.id.toString()),
                )
                .name(name.toString())
                .amountInCents(p)
                .type(DbTransaction.Type.SPEND)
                .note(
                    StringBuilder("Automatically created from Notification")
                        .newline()
                        .append(pt)
                        .newline()
                        .run {
                          if (appName != null) {
                            append("From App: $appName")
                          } else {
                            this
                          }
                        }
                        .toString())

        transactionInsertDao.requireNotNull().insert(transaction).also {
          when (it) {
            is DbInsert.InsertResult.Fail -> {
              Timber.e(it.error, "Failed to insert automatic transaction $transaction")
            }
            is DbInsert.InsertResult.Insert -> {
              Timber.d("Inserted automatic transaction: $transaction")
            }
            is DbInsert.InsertResult.Update -> {
              Timber.d("Update existing automatic transaction: $transaction")
            }
          }
        }
      }
    }
  }

  @CheckResult
  private fun StringBuilder.newline(): StringBuilder {
    return this.append('\n')
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
}
