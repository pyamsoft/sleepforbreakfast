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

package com.pyamsoft.sleepforbreakfast.spending.automatic.handlers

import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_GROUP_AMOUNT
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_ACCOUNT
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_DATE
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_MERCHANT
import com.pyamsoft.sleepforbreakfast.spending.automatic.SpendAutomaticHandler
import javax.inject.Inject

/**
 * Chase Bank app can be set up in Alert settings to post a notification each time you spend X
 * amount on a card.
 */
internal class ChaseBankAppSpend @Inject internal constructor() : SpendAutomaticHandler() {

  override fun getPossibleRegexes() = listOf(CHASE_ALERT_EMAIL_REGEX)

  override fun canExtract(packageName: String): Boolean {
    return packageName == "com.chase.sig.android"
  }

  companion object {

    private const val ACCOUNT_GROUP = "(?<$CAPTURE_NAME_ACCOUNT>Chase .*)"
    private const val DATE_GROUP =
        "(?<$CAPTURE_NAME_DATE>\\w*\\s\\w*,\\s\\w*\\sat\\s\\w*:\\w*\\s\\w*\\s\\w*)"
    private const val MERCHANT_GROUP = "(?<$CAPTURE_NAME_MERCHANT>.*)"

    /**
     * You made an online, phone, or mail transaction
     *
     * Chase Freedom: You made an online, phone, or mail transaction of $2.00 with My Favorite
     * Merchant on Oct 7, 2023 at 1:23PM ET
     */
    private val CHASE_ALERT_EMAIL_REGEX =
        "${ACCOUNT_GROUP}: You made an online, phone, or mail transaction of $CAPTURE_GROUP_AMOUNT with $MERCHANT_GROUP on ${DATE_GROUP}."
            .toRegex(RegexOption.MULTILINE)
  }
}
