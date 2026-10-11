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

package com.pyamsoft.sleepforbreakfast.home.notification

import android.content.Intent
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.annotation.CheckResult
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.util.AppDispatchers
import com.pyamsoft.sleepforbreakfast.core.Timber
import javax.inject.Inject
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

internal class NotificationListenerStatusImpl
@Inject
internal constructor(
    private val activity: ComponentActivity,
    private val dispatchers: AppDispatchers,
) : NotificationListenerStatus {

  @CheckResult
  private fun isNotificationListenerEnabled(): Boolean {
    val context = activity.applicationContext
    val listeners = NotificationManagerCompat.getEnabledListenerPackages(context)
    return listeners.contains(context.packageName)
  }

  @CheckResult
  private fun tryOpenIntent(intent: Intent): Boolean {
    return try {
      activity.startActivity(intent)
      true
    } catch (e: android.content.ActivityNotFoundException) {
      Timber.e(e) { "Could not open intent: ${intent.action}" }
      false
    }
  }

  @CheckResult
  private fun openSettingsPageIntent(action: String): Boolean {
    // Try specific first, may fail on some devices
    var intent = Intent(action, "package:${activity.packageName}".toUri())
    if (!tryOpenIntent(intent)) {
      Timber.w { "Failed specific intent for $action" }
      intent = Intent(action)
      if (!tryOpenIntent(intent)) {
        Timber.w { "Failed generic intent for $action" }
        return false
      }
    }

    return true
  }

  override fun isNotificationListenerActive(): Flow<Boolean> {
    return callbackFlow {
          val observer =
              object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                  trySend(isNotificationListenerEnabled())
                }
              }

          activity.lifecycle.addObserver(observer)
          awaitClose { activity.lifecycle.removeObserver(observer) }
        }
        .flowOn(context = dispatchers.main)
  }

  override suspend fun activateNotificationListener() =
      withContext(context = dispatchers.default) {
        val action = Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
        if (openSettingsPageIntent(action)) {
          Timber.w { "Failed to open settings page: $action" }
        }
      }
}
