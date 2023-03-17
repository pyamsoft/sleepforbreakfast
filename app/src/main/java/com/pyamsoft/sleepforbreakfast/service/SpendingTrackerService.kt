package com.pyamsoft.sleepforbreakfast.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.spending.SpendingTrackerHandler
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber

class SpendingTrackerService : NotificationListenerService() {
  private var scope: CoroutineScope? = null

  @Inject @JvmField internal var handler: SpendingTrackerHandler? = null

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

    handler = null
  }

  override fun onNotificationPosted(sbn: StatusBarNotification) {
    withScope { launch(context = Dispatchers.IO) { processNotification(sbn) } }
  }

  private fun ensureInjected() {
    if (handler == null) {
      ObjectGraph.ApplicationScope.retrieve(application).inject(this)
    }
  }

  private suspend fun processNotification(sbn: StatusBarNotification) {
    Enforcer.assertOffMainThread()

    ensureInjected()

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

    handler?.processNotification(sbn, extras)
  }
}
