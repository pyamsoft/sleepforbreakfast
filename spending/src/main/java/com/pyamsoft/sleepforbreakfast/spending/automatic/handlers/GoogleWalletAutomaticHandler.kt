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
import com.pyamsoft.sleepforbreakfast.spending.automatic.SpendAutomaticHandler
import javax.inject.Inject

/** Google wallet notifications come from Google Play Services */
internal class GoogleWalletAutomaticHandler @Inject internal constructor() :
    SpendAutomaticHandler() {

  override fun getRegex(): Regex {
    return GOOGLE_WALLET_REGEX
  }

  override fun canExtract(packageName: String): Boolean {
    return packageName == "com.google.android.gms"
  }

  override suspend fun getCategories(): List<DbCategory.Id> {
    return emptyList()
  }

  companion object {

    private const val ACCOUNT_GROUP = "(?<$CAPTURE_NAME_ACCOUNT>.* •••• \\d\\d\\d\\d)"

    /**
     * Google Wallet posts messages like
     *
     * $123.45 with Amex •••• 1234
     *
     * We can look for that notification text and parse the values out
     */
    private val GOOGLE_WALLET_REGEX =
        "$CAPTURE_GROUP_AMOUNT with $ACCOUNT_GROUP".toRegex(RegexOption.MULTILINE)
  }
}