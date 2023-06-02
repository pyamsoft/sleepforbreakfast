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

package com.pyamsoft.sleepforbreakfast.service

import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
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

  private fun ensureInjected() {
    if (handler == null) {
      ObjectGraph.ApplicationScope.retrieve(application).inject(this)
    }
  }

  override fun onListenerConnected() {
    scope = scope ?: MainScope()
  }

  override fun onDestroy() {
    scope?.cancel()
    scope = null
    handler = null
  }

  override fun onNotificationPosted(sbn: StatusBarNotification) {
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

    ensureInjected()
    scope?.apply { launch(context = Dispatchers.Default) { handler?.processNotification(sbn, extras) } }
  }
}
