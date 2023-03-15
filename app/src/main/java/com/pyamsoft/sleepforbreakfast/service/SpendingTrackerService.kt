package com.pyamsoft.sleepforbreakfast.service

import android.content.pm.ApplicationInfo
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import com.pyamsoft.pydroid.core.Enforcer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

class SpendingTrackerService : NotificationListenerService() {
  private var scope: CoroutineScope? = null

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
  }

  override fun onNotificationPosted(sbn: StatusBarNotification) {
    withScope { launch(context = Dispatchers.IO) { processNotification(sbn) } }
  }

  private fun processNotification(sbn: StatusBarNotification) {
    Enforcer.assertOffMainThread()

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
    val title = extras.getCharSequence(NotificationCompat.EXTRA_TITLE, "")
    val bigTitle = extras.getCharSequence(NotificationCompat.EXTRA_TITLE_BIG, "")
    val appInfo = extras.resolveParcelable<ApplicationInfo>("android.appInfo")

    Timber.d(
        "Notification received: ${mapOf(
            "Text" to text,
            "Big Text" to bigText,
            "Title" to title,
            "Big Title" to bigTitle,
            "App Info" to appInfo,
            "App Label" to appInfo?.let {
              try {
                application.packageManager.getApplicationLabel(it) } catch (e: Throwable) {
                Timber.e(e, "Error loading AppInfo label")
              }
            },
            "App Icon" to appInfo?.let {
              try {
                application.packageManager.getApplicationIcon(it)
              } catch (e: Throwable) {
                Timber.e(e, "Error loading AppInfo Icon")
              }
            },
          )}")
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
