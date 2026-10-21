/*
 * Copyright 2026 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.service

import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.util.AppDispatchers
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.spending.SpendingTrackerHandler
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

class SpendingTrackerService : NotificationListenerService() {

  private val serviceComponentName: ComponentName by lazy {
    ComponentName(applicationContext, SpendingTrackerService::class.java)
  }

  @Inject @JvmField internal var handler: SpendingTrackerHandler? = null

  @Inject @JvmField internal var dispatchers: AppDispatchers? = null

  private var scope: CoroutineScope? = null
  private var isDestroying = false

  private fun ensureScope(): CoroutineScope {
    return scope
        ?: CoroutineScope(
                context =
                    SupervisorJob() +
                        dispatchers.requireNotNull().default +
                        CoroutineName(this::class.java.name),
            )
            .also { scope = it }
  }

  private fun ensureInjected() {
    if (handler == null) {
      ObjectGraph.ApplicationScope.retrieve(application).inject(this)
    }
  }

  private fun processNotification(sbn: StatusBarNotification) {
    val notif = sbn.notification
    if (notif == null) {
      Timber.w { "SBN is missing notification data" }
      return
    }

    val extras = notif.extras
    if (extras == null) {
      Timber.w { "SBN Notification data is missing extras bundle" }
      return
    }

    ensureInjected()
    handler?.also { h -> ensureScope().launch { h.processNotification(sbn, extras) } }
  }

  override fun onListenerConnected() {
    Timber.d { "Notification listener service is connected!" }
    ensureInjected()

    // Re-process all current notifications
    val allNotifs = activeNotifications
    if (allNotifs != null) {
      allNotifs.forEach { processNotification(it) }
    }
  }

  override fun onListenerDisconnected() {
    if (!isDestroying) {
      Timber.d { "Listener has disconnected, request rebind()" }
      requestRebind(serviceComponentName)
    }
  }

  override fun onDestroy() {
    // Mark before super, which runs onListenerDisconnected()
    isDestroying = true

    super.onDestroy()
    scope?.cancel()

    scope = null
    handler = null
    dispatchers = null
  }

  override fun onNotificationPosted(sbn: StatusBarNotification) {
    ensureInjected()
    processNotification(sbn)
  }
}
