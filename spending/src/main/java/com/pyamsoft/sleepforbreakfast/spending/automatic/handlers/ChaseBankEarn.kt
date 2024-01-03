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
import com.pyamsoft.sleepforbreakfast.spending.automatic.COMMON_EMAIL_PACKAGES
import com.pyamsoft.sleepforbreakfast.spending.automatic.EarnAutomaticHandler
import javax.inject.Inject

/**
 * Chase Bank can be configured to send email alerts when you receive direct deposit into an account
 */
internal class ChaseBankEarn @Inject internal constructor() : EarnAutomaticHandler() {

  override fun getPossibleRegexes() = DIRECT_DEPOSITS

  override fun canExtract(packageName: String): Boolean {
    return if (WATCH_APP) {
      packageName == "com.chase.sig.android"
    } else {
      packageName in COMMON_EMAIL_PACKAGES
    }
  }

  companion object {

    /**
     * Watch the Chase App instead of email stream
     *
     * Sometimes the chase app doesn't post a push notification but we always get emails
     */
    private const val WATCH_APP = false

    private const val ACCOUNT_GROUP = "(?<$CAPTURE_NAME_ACCOUNT>\\.\\.\\.\\d\\d\\d\\d)"
    private const val DATE_GROUP =
        "(?<$CAPTURE_NAME_DATE>\\w*\\s\\w*,\\s\\w*\\sat\\s\\w*:\\w*\\s\\w*\\s\\w*)"

    private val DIRECT_DEPOSITS =
        setOf(
            /**
             * From Chase App
             *
             * Deposit posted You have a direct deposit of $123.45 Account ending in (...1234)
             * Posted Oct 12, 2023 at 5:37 AM ET Amount $123.45
             */
            "Deposit posted .* Account ending in \\($ACCOUNT_GROUP\\) Posted $DATE_GROUP Amount $CAPTURE_GROUP_AMOUNT"
                .toRegex(RegexOption.MULTILINE),
        )
  }
}