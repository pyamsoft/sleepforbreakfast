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
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_DESCRIPTION
import com.pyamsoft.sleepforbreakfast.spending.automatic.CAPTURE_NAME_MERCHANT
import com.pyamsoft.sleepforbreakfast.spending.automatic.EarnAutomaticHandler
import javax.inject.Inject

/** When you pay someone on Venmo but they request you to */
internal class VenmoReceiveRequested @Inject internal constructor() : EarnAutomaticHandler() {

  override fun getPossibleRegexes() = listOf(VENMO_WALLET_REGEX)

  override fun canExtract(packageName: String): Boolean {
    return packageName == "com.venmo"
  }

  companion object {

    private const val MERCHANT_GROUP = "(?<$CAPTURE_NAME_MERCHANT>.*)"
    private const val DESCRIPTION_GROUP = "(?<$CAPTURE_NAME_DESCRIPTION>.*)"

    /** Tom paid you $123.45 - Note about payment here - You now have $250 in your Venmo account */
    private val VENMO_WALLET_REGEX =
        "$MERCHANT_GROUP paid you $CAPTURE_GROUP_AMOUNT - $DESCRIPTION_GROUP - You now have \$"
            .toRegex(RegexOption.MULTILINE)
  }
}
