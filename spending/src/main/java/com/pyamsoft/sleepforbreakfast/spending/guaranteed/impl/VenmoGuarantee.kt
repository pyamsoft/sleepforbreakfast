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
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_DESCRIPTION
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_MERCHANT
import com.pyamsoft.sleepforbreakfast.spending.automatic.COMMON_EMAIL_PACKAGES
import com.pyamsoft.sleepforbreakfast.spending.guaranteed.BaseGuarantee
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class VenmoGuarantee
@Inject
internal constructor(
    private val clock: Clock,
) : BaseGuarantee() {

  private val chaseSpend by lazy {
    val notificationId = DbNotification.Id("f3e642e8-7af4-4a51-87a1-e5d4693280bd")
    DbNotificationWithRegexes.create(
        notification =
            DbNotification.create(
                system = true,
                clock = clock,
                id = notificationId,
                name = "Chase Bank Spending",
                actOnPackageNames = setOf(*COMMON_EMAIL_PACKAGES.toTypedArray(), "com.venmo"),
                type = DbTransaction.Type.SPEND,
            ),
        regexes =
            setOf(
                /**
                 * From Venmo App You Pay Someone Prompted
                 *
                 * You completed Tom Smith's request for $123.45 - Note about payment here
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("ed18b2be-4933-44f4-b52e-8ec5f52a7294"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "You completed $MERCHANT_GROUP's request for $CAPTURE_GROUP_AMOUNT - $DESCRIPTION_GROUP",
                ),

                /**
                 * From Venmo App You Pay Someone Unprompted
                 *
                 * You paid Tom Smith $123.45
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("6c9a736c-bc90-405b-bbc5-1d287073e0a8"),
                    clock = clock,
                    notificationId = notificationId,
                    text = "You paid $MERCHANT_GROUP $CAPTURE_GROUP_AMOUNT",
                ),
            ),
    )
  }

  private val chaseEarn by lazy {
    val notificationId = DbNotification.Id("a1b3e29b-5d95-45a5-b824-ae737ed58bda")
    DbNotificationWithRegexes.create(
        notification =
            DbNotification.create(
                system = true,
                clock = clock,
                id = notificationId,
                name = "Venmo Earning",
                actOnPackageNames = setOf(*COMMON_EMAIL_PACKAGES.toTypedArray(), "com.venmo"),
                type = DbTransaction.Type.EARN,
            ),
        regexes =
            setOf(
                /**
                 * From Venmo App Payment Requested Prompted
                 *
                 * Tom paid you $123.45. - Note about payment here - You now have $250 in your Venmo
                 * account
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("92c52e07-3da6-450d-923c-43fe6bc77041"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "$MERCHANT_GROUP paid you $CAPTURE_GROUP_AMOUNT. - $DESCRIPTION_GROUP - You now have \$",
                ),

                /**
                 * From Venmo App Payment Requested Unprompted
                 *
                 * Tom completed your request for request for $123.45 - Note about payment here
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("cdbf54a9-0398-4796-9af0-1faaeba9e7dc"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "$MERCHANT_GROUP completed your request for $CAPTURE_GROUP_AMOUNT - $DESCRIPTION_GROUP",
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
            notification = chaseEarn,
        )
        upsertIfUntainted(
            query = query,
            insert = insert,
            notification = chaseSpend,
        )
      }

  companion object {

    private const val MERCHANT_GROUP = "(?<$CAPTURE_NAME_MERCHANT>.*)"
    private const val DESCRIPTION_GROUP = "(?<$CAPTURE_NAME_DESCRIPTION>.*)"
  }
}
