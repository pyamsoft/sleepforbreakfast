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

package com.pyamsoft.sleepforbreakfast.spending.guaranteed.impl

import com.pyamsoft.sleepforbreakfast.db.notification.DbNotification
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationMatchRegex
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationInsertDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_GROUP_AMOUNT
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_ACCOUNT
import com.pyamsoft.sleepforbreakfast.spending.guaranteed.BaseGuarantee
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class GoogleWalletGuarantee
@Inject
internal constructor(
    private val clock: Clock,
) : BaseGuarantee() {

  private val googleWalletSpend by lazy {
    val notificationId = DbNotification.Id("05d18a04-db29-418d-b363-5240b2f5acfc")
    DbNotificationWithRegexes.create(
        notification =
            DbNotification.create(
                clock = clock,
                id = notificationId,
                name = "Google Wallet Spending",
                actOnPackageNames = setOf("com.google.android.gms"),
                type = DbTransaction.Type.SPEND,
            ),
        regexes =
            setOf(
                /**
                 * From Google Wallet App
                 *
                 * $123.45 with Amex •••• 1234
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("38875bfe-8dbc-49e4-9732-9a50b08dd588"),
                    clock = clock,
                    notificationId = notificationId,
                    text = "$CAPTURE_GROUP_AMOUNT with $ACCOUNT_GROUP",
                ),
            ),
    )
  }

  override suspend fun ensureExistsInDatabase(
      query: NotificationQueryDao,
      insert: NotificationInsertDao
  ) =
      withContext(context = Dispatchers.Default) {
        upsertIfUntainted(
            query = query,
            insert = insert,
            notification = googleWalletSpend,
        )
      }

  companion object {

    private const val ACCOUNT_GROUP = "(?<$CAPTURE_NAME_ACCOUNT>.* •••• \\d\\d\\d\\d)"
  }
}
