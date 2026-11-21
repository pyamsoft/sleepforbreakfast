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
import com.pyamsoft.sleepforbreakfast.ui.spending.SpendingServiceHelper
import javax.inject.Inject
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class SpendingTrackerService : NotificationListenerService() {

  private val serviceComponentName: ComponentName by lazy {
    ComponentName(applicationContext, SpendingTrackerService::class.java)
  }

  @Inject @JvmField internal var handler: SpendingTrackerHandler? = null

  @Inject @JvmField internal var dispatchers: AppDispatchers? = null

  @Inject @JvmField internal var helper: SpendingServiceHelper? = null

  private var scope: CoroutineScope? = null

  // We are actually going down and do not want to rebind
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

  private fun processNotification(
      scope: CoroutineScope,
      sbn: StatusBarNotification,
  ) {
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
    handler?.also { h -> scope.launch { h.processNotification(sbn, extras) } }
  }

  private fun replayAllActiveNotifications(scope: CoroutineScope) {
    val allNotifs = activeNotifications
    if (allNotifs != null) {
      allNotifs.forEach { processNotification(scope, it) }
    }
  }

  override fun onCreate() {
    super.onCreate()
    Timber.d { "Notification listener service is created!" }
    ensureInjected()
  }

  override fun onListenerConnected() {
    Timber.d { "Notification listener service is connected!" }
    ensureInjected()

    val scope = ensureScope()

    replayAllActiveNotifications(scope)
  }

  override fun onListenerDisconnected() {
    if (!isDestroying) {
      Timber.d { "Listener has disconnected, request rebind()" }
      requestRebind(serviceComponentName)
    }
  }

  override fun onDestroy() {
    var rebind = false
    val h = helper

    if (h == null) {
      Timber.w { "Helper was NULL, we never injected? Assume dead" }
      isDestroying = true
    } else {
      if (h.isNotificationListenerEnabled()) {
        Timber.d { "fresh: onDestroy called but listener is still enabled. Request rebind!" }
        rebind = true
        isDestroying = false
      } else {
        Timber.d { "fresh: Notification Listener was disabled, stop service!" }
        // Mark before super, which runs onListenerDisconnected()
        isDestroying = true
      }
    }

    super.onDestroy()

    scope?.cancel()

    scope = null
    helper = null
    handler = null
    dispatchers = null

    if (rebind) {
      Timber.d { "Request rebind after destroy!" }
      requestRebind(serviceComponentName)
    }
  }

  override fun onNotificationPosted(sbn: StatusBarNotification) {
    ensureInjected()

    val scope = ensureScope()
    processNotification(scope, sbn)
  }
}
