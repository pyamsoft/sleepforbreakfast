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

import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_GROUP_AMOUNT
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_ACCOUNT
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_DATE
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_MERCHANT
import com.pyamsoft.sleepforbreakfast.spending.automatic.SpendAutomaticHandler
import javax.inject.Inject

/**
 * Chase Bank sends "Alert" emails when a card is used if your account is configured.
 *
 * The arrive over email clients in a generally expected format
 */
internal class ChaseBankEmailAutomaticHandler @Inject internal constructor() :
    SpendAutomaticHandler() {

  override fun getRegex(): Regex {
    return CHASE_ALERT_EMAIL_REGEX
  }

  override fun canExtract(packageName: String): Boolean {
    return packageName == "com.chase.sig.android"
  }

  override suspend fun getCategories(): List<DbCategory.Id> {
    return emptyList()
  }

  companion object {

    private const val ACCOUNT_GROUP = "(?<$CAPTURE_NAME_ACCOUNT>Chase .*)"
    private const val DATE_GROUP =
        "(?<$CAPTURE_NAME_DATE>\\w*\\s\\w*,\\s\\w*\\sat\\s\\w*:\\w*\\s\\w*\\s\\w*)"
    private const val MERCHANT_GROUP = "(?<$CAPTURE_NAME_MERCHANT>.*)"

    /**
     * Chase sends emails like
     * ==
     * You made an online, phone, or mail transaction
     *
     * Chase Freedom: You made a $2.00 transaction with My Favorite Merchant on Oct 7, 2023 at
     * 1:23PM ET
     * ==
     * We can look for that notification text and parse the values out
     */
    private val CHASE_ALERT_EMAIL_REGEX =
        "${ACCOUNT_GROUP}: You made a $CAPTURE_GROUP_AMOUNT transaction with $MERCHANT_GROUP on ${DATE_GROUP}."
            .toRegex(RegexOption.MULTILINE)
  }
}
