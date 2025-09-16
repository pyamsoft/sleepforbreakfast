/*
 * Copyright 2025 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.spending.automatic

import android.os.Bundle
import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationQueryDao
import com.pyamsoft.sleepforbreakfast.spending.AutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.InternalApi
import com.pyamsoft.sleepforbreakfast.spending.PaymentNotification
import com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.AutomaticIgnores
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AutomaticManagerImpl
@Inject
internal constructor(
    private val queryDao: NotificationQueryDao,
    @param:InternalApi private val ignores: AutomaticIgnores,
) : AutomaticManager {

  @CheckResult
  private suspend fun collectNotificationHandlers(): Collection<AutomaticHandler> {
    return queryDao.query().map {
      DbAutomaticHandler.create(
          notification = it,
          ignores = ignores,
      )
    }
  }

  override suspend fun extractPayment(
      notificationId: Int,
      packageName: String,
      bundle: Bundle,
  ): PaymentNotification? {
    // This loop continues until we find a result since multiple handlers may
    // handle the same packagename, like Venmo
    val handlers = collectNotificationHandlers()

    for (handler in handlers) {
      if (handler.canExtract(packageName) || TEST_CAN_ALWAYS_EXTRACT) {
        val result = handler.extract(notificationId, packageName, bundle)
        if (result != null) {
          return result
        }
      }
    }

    return null
  }

  companion object {

    /** Test the regex system by letting it run against ANY package */
    private const val TEST_CAN_ALWAYS_EXTRACT = true
  }
}
