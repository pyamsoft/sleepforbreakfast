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

package com.pyamsoft.sleepforbreakfast.spending.automatic.googlewallet

import com.pyamsoft.sleepforbreakfast.core.RAW_STRING_DOLLAR_PRICE
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.category.system.SystemCategories
import com.pyamsoft.sleepforbreakfast.spending.automatic.SpendAutomaticHandler
import javax.inject.Inject
import javax.inject.Singleton

/** Google wallet notifications come from Google Play Services */
@Singleton
internal class GoogleWalletAutomaticHandler
@Inject
internal constructor(
    private val systemCategories: SystemCategories,
) : SpendAutomaticHandler() {

  override fun getRegex(): Regex {
    return GOOGLE_WALLET_REGEX
  }

  override fun canExtract(packageName: String): Boolean {
    return packageName == "com.google.android.gms"
  }

  override suspend fun getCategories(): List<DbCategory.Id> {
    val google =
        systemCategories.categoryByName(SystemCategories.Categories.GOOGLE_WALLET)
            ?: return emptyList()

    return listOf(google.id)
  }

  companion object {

    /**
     * Google Wallet posts messages like
     *
     * $123.45 with Amex •••• 1234
     *
     * We can look for that notification text and parse the values out
     */
    private val GOOGLE_WALLET_REGEX =
        "$RAW_STRING_DOLLAR_PRICE with .* ••••".toRegex(RegexOption.MULTILINE)
  }
}
