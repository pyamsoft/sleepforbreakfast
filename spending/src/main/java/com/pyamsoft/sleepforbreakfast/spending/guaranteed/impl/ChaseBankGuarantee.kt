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

package com.pyamsoft.sleepforbreakfast.spending.guaranteed.impl

import com.pyamsoft.sleepforbreakfast.db.notification.DbNotification
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationMatchRegex
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationInsertDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_GROUP_AMOUNT
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_ACCOUNT
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_DATE
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_MERCHANT
import com.pyamsoft.sleepforbreakfast.spending.automatic.COMMON_EMAIL_PACKAGES
import com.pyamsoft.sleepforbreakfast.spending.guaranteed.BaseGuarantee
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
internal class ChaseBankGuarantee
@Inject
internal constructor(
    private val clock: Clock,
) : BaseGuarantee() {

  private val chaseSpend by lazy {
    val notificationId = DbNotification.Id("16bdd569-b438-42d9-b118-8849d2998934")
    DbNotificationWithRegexes.create(
        notification =
            DbNotification.create(
                system = true,
                clock = clock,
                id = notificationId,
                name = "Chase Bank Spending",
                actOnPackageNames =
                    setOf(
                        *COMMON_EMAIL_PACKAGES.toTypedArray(), /*"com.chase.sig.android"*/
                    ),
                type = DbTransaction.Type.SPEND,
            ),
        regexes =
            setOf(
                /**
                 * From Chase App Credit Card
                 *
                 * Chase Freedom: You made an online, phone, or mail transaction of $2.00 with My
                 * Favorite Merchant on Oct 7, 2023 at 1:23PM ET
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("49a223d5-68bd-4023-83fd-942567ad0ef5"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "$CHASE_PREFIXED_ACCOUNT_GROUP: You made an online, phone, or mail transaction of $CAPTURE_GROUP_AMOUNT with $MERCHANT_GROUP on $DATE_GROUP.",
                ),

                /**
                 * From Chase App Credit Card
                 *
                 * Chase Freedom: You made a $2.00 transaction with My Favorite Merchant on Oct 7,
                 * 2023 at 1:23PM ET
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("647c8a72-ed9d-4e62-8393-feca7068c067"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "$CHASE_PREFIXED_ACCOUNT_GROUP: You made a $CAPTURE_GROUP_AMOUNT transaction with $MERCHANT_GROUP on $DATE_GROUP.",
                ),

                /**
                 * From Email Credit Card
                 *
                 * You made a $2.00 transaction Account Chase Freedom (...1234) Date Oct 7, 2023 at
                 * 1:23PM ET Merchant My Favorite Merchant Amount
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("3dd63de5-06bc-41e1-86b8-dc52bc4830bc"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "You made a $CAPTURE_GROUP_AMOUNT transaction Account $CHASE_PREFIXED_ACCOUNT_GROUP Date $DATE_GROUP Merchant $MERCHANT_GROUP Amount",
                ),

                /**
                 * From Chase App Debit Card
                 *
                 * Chase account 1234: Your $12.34 debit card transaction to MERCHANT MAN on Oct 20,
                 * 2023 at 10:13AM ET was more than the $1.00 amount in your Alerts settings 1:23PM
                 * ET
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("78f25cea-04e2-448d-87e0-13ccd712109c"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "$CHASE_PREFIXED_ACCOUNT_GROUP: Your $CAPTURE_GROUP_AMOUNT debit card transaction to $MERCHANT_GROUP on $DATE_GROUP was more than the",
                ),

                /**
                 * From Chase App Debit Card
                 *
                 * Chase account 1234: You made a $12.34 debit card transaction to MERCHANT MAN on
                 * Oct 20, 2023 at 10:13AM ET was more than the $1.00 amount in your Alerts settings
                 * 1:23PM ET
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("f5e2e797-4f40-4534-a2b9-217f37a16909"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "$CHASE_PREFIXED_ACCOUNT_GROUP: You made a $CAPTURE_GROUP_AMOUNT debit card transaction to $MERCHANT_GROUP on $DATE_GROUP was more than the",
                ),

                /**
                 * From Email Debit Card
                 *
                 * Your debit card transaction of $12.34 with My Favorite Merchant Account ending in
                 * (...1234) Made on 2023 at 10:13AM ET
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("0bb2b4dd-7c34-4ad0-b94e-6abcb668a906"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "Your debit card transaction of $CAPTURE_GROUP_AMOUNT with $MERCHANT_GROUP Account ending in $PLAIN_ACCOUNT_GROUP Made on $DATE_GROUP",
                ),

                /**
                 * From Email Debit Card
                 *
                 * You made a debit card transaction of $12.34 with My Favorite Merchant Account
                 * ending in (...1234) Made on 2023 at 10:13AM ET
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("0b36cb4f-52f2-44e3-bf4c-2d269161f831"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "You made a debit card transaction of $CAPTURE_GROUP_AMOUNT with $MERCHANT_GROUP Account ending in $PLAIN_ACCOUNT_GROUP Made on $DATE_GROUP",
                ),
            ),
    )
  }

  private val chaseEarn by lazy {
    val notificationId = DbNotification.Id("9b71c5de-a4ec-4470-a6b2-673f310d9ad5")
    DbNotificationWithRegexes.create(
        notification =
            DbNotification.create(
                system = true,
                clock = clock,
                id = notificationId,
                name = "Chase Bank Earning",
                actOnPackageNames =
                    setOf(
                        *COMMON_EMAIL_PACKAGES.toTypedArray(), /*"com.chase.sig.android"*/
                    ),
                type = DbTransaction.Type.EARN,
            ),
        regexes =
            setOf(
                /**
                 * From Chase App Direct Deposit
                 *
                 * Deposit posted You have a direct deposit of $123.45 Account ending in (...1234)
                 * Posted Oct 12, 2023 at 5:37 AM ET Amount $123.45
                 */
                DbNotificationMatchRegex.create(
                    id = DbNotificationMatchRegex.Id("3560841b-6798-4bfc-9ad2-9335df04f4d6"),
                    clock = clock,
                    notificationId = notificationId,
                    text =
                        "Deposit posted .* Account ending in $PLAIN_ACCOUNT_GROUP Posted $DATE_GROUP Amount $CAPTURE_GROUP_AMOUNT",
                ),
            ),
    )
  }

  override suspend fun ensureExistsInDatabase(
      query: NotificationQueryDao,
      insert: NotificationInsertDao,
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

    private const val PLAIN_ACCOUNT_GROUP = "(?<$CAPTURE_NAME_ACCOUNT>\\(.*\\d\\d\\d\\d\\))"
    private const val CHASE_PREFIXED_ACCOUNT_GROUP = "(?<$CAPTURE_NAME_ACCOUNT>Chase .*)"

    private const val DATE_GROUP =
        "(?<$CAPTURE_NAME_DATE>\\w*\\s\\w*,\\s\\w*\\sat\\s\\w*:\\w*\\s\\w*\\s\\w*)"
    private const val MERCHANT_GROUP = "(?<$CAPTURE_NAME_MERCHANT>.*)"
  }
}
