/*
 * Copyright 2024 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.spending

import android.os.Bundle
import androidx.core.app.NotificationCompat
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationMatchRegex
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.spending.automatic.AutomaticManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
internal class DefaultSpendingTester
@Inject
internal constructor(
    private val manager: AutomaticManager,
) : SpendingTester {

  override suspend fun testText(
      notificationWithRegexes: DbNotificationWithRegexes,
      text: String
  ): SpendingTester.Result? =
      GLOBAL_LOCK.withLock {
        val notification = notificationWithRegexes.notification
        val packageName = notification.actOnPackageNames.firstOrNull()

        if (packageName == null) {
          Timber.w { "Cannot test regex, no package names!" }
          return@withLock null
        }

        val bundle = Bundle().apply { putCharSequence(NotificationCompat.EXTRA_TEXT, text) }
        val automaticPayment = manager.extractPayment(notificationId = 0, packageName, bundle)
        if (automaticPayment == null) {
          Timber.w { "Regex test failed, no matches!" }
          return@withLock null
        }

        return@withLock SpendingTester.Result(
            notification = notification.id,
            matching = setOf(DbNotificationMatchRegex.Id(automaticPayment.regexMatch.id)),
        )
      }

  companion object {

    /**
     * the global lock prevents multiple callers from running this handler at the same time as it
     * could cause duplicates in the DB if operations are close enough
     */
    private val GLOBAL_LOCK = Mutex()
  }
}
