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

package com.pyamsoft.sleepforbreakfast.spending.guaranteed.impl

import com.pyamsoft.pydroid.util.AppDispatchers
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotification
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationMatchRegex
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationInsertDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_GROUP_AMOUNT
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_ACCOUNT
import com.pyamsoft.sleepforbreakfast.spending.guaranteed.BaseGuarantee
import kotlinx.coroutines.withContext
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class GoogleWalletGuarantee
@Inject
internal constructor(
    private val clock: Clock,
    dispatchers: AppDispatchers,
) :
    BaseGuarantee(
        dispatchers = dispatchers,
    ) {

  private val googleWalletSpend by lazy {
    val notificationId = DbNotification.Id("05d18a04-db29-418d-b363-5240b2f5acfc")
    DbNotificationWithRegexes.create(
        notification =
            DbNotification.create(
                system = true,
                clock = clock,
                id = notificationId,
                name = "Google Wallet Spending",
                actOnPackageNames =
                    setOf(
                        "com.google.android.gms",
                        "com.google.android.apps.walletnfcrel",
                    ),
                type = DbTransaction.Type.SPEND,
            ),
        regexes =
            setOf(
                /**
                 * From Google Wallet App
                 *
                 * $123.45 with Amex •••• 1234
                 *
                 * 09/19/2025 noticed Wallet notification updated to $123.45 with Amex ••1234
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("38875bfe-8dbc-49e4-9732-9a50b08dd588"),
                    clock = clock,
                    notificationId = notificationId,
                    text = "$CAPTURE_GROUP_AMOUNT\\s+with\\s+$ACCOUNT_GROUP",
                ),
            ),
    )
  }

  private val googleWalletEarn by lazy {
    val notificationId = DbNotification.Id("def80da2-9ff6-47ea-9089-cd457c0cd52e")
    DbNotificationWithRegexes.create(
        notification =
            DbNotification.create(
                system = true,
                clock = clock,
                id = notificationId,
                name = "Google Wallet Earning",
                actOnPackageNames =
                    setOf(
                        "com.google.android.gms",
                        "com.google.android.apps.walletnfcrel",
                    ),
                type = DbTransaction.Type.SPEND,
            ),
        regexes =
            setOf(
                /**
                 * From Google Wallet App
                 *
                 * -$123.45 was refunded to Amex ••1234
                 *
                 * 09/19/2025 noticed Wallet notification updated to $123.45 with Amex ••1234
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("51a3da0e-61dc-4488-9108-0a6cdaffd437"),
                    clock = clock,
                    notificationId = notificationId,
                    text = "-$CAPTURE_GROUP_AMOUNT\\s+was\\s+refunded\\s+to\\s+$ACCOUNT_GROUP",
                ),
            ),
    )
  }

  override suspend fun ensureExistsInDatabase(
      query: NotificationQueryDao,
      insert: NotificationInsertDao,
  ) =
      withContext(context = dispatchers.default) {
        upsertIfUntainted(
            query = query,
            insert = insert,
            notification = googleWalletSpend,
        )

        upsertIfUntainted(
            query = query,
            insert = insert,
            notification = googleWalletEarn,
        )
      }

  companion object {

    // CardName <space> <1 to 4 •> <maybe a space, but possibly no space> <4 digits>
    // For example
    // Chase Freedom ••1234
    // Chase Freedom •• 1234
    // Chase Freedom ••••1234
    // Chase Freedom •••• 1234
    private const val ACCOUNT_GROUP = "(?<$CAPTURE_NAME_ACCOUNT>.*\\s+•{1,4}\\s*\\d{4})"
  }
}
