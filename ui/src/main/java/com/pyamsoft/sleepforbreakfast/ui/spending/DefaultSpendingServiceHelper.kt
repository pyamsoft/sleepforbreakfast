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

package com.pyamsoft.sleepforbreakfast.ui.spending

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.pyamsoft.pydroid.util.AppDispatchers
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn

@Singleton
internal class DefaultSpendingServiceHelper
@Inject
internal constructor(
    private val appContext: Context,
    private val dispatchers: AppDispatchers,
) : SpendingServiceHelper {

  override fun isNotificationListenerEnabled(): Boolean {
    val listeners = NotificationManagerCompat.getEnabledListenerPackages(appContext)
    return listeners.contains(appContext.packageName)
  }

  override fun listenForNotificationListenerStatus(lifecycle: Lifecycle): Flow<Boolean> {
    return callbackFlow {
          val observer =
              object : DefaultLifecycleObserver {

                override fun onResume(owner: LifecycleOwner) {
                  trySend(isNotificationListenerEnabled())
                }
              }

          lifecycle.addObserver(observer)
          awaitClose { lifecycle.removeObserver(observer) }
        }
        .flowOn(context = dispatchers.main)
  }
}
