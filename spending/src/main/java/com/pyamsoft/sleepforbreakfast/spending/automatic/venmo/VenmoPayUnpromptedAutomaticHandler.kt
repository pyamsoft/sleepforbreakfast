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

package com.pyamsoft.sleepforbreakfast.spending.automatic.venmo

import com.pyamsoft.sleepforbreakfast.core.RAW_STRING_DOLLAR_PRICE
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.category.system.SystemCategories
import com.pyamsoft.sleepforbreakfast.spending.automatic.SpendAutomaticHandler
import javax.inject.Inject
import javax.inject.Singleton

/** When you pay someone on Venmo without them requesting you first */
@Singleton
internal class VenmoPayUnpromptedAutomaticHandler
@Inject
internal constructor(
    private val systemCategories: SystemCategories,
) : SpendAutomaticHandler() {

  override fun getRegex(): Regex {
    return VENMO_WALLET_REGEX
  }

  override fun canExtract(packageName: String): Boolean {
    return packageName == "com.venmo"
  }

  override suspend fun getCategories(): List<DbCategory.Id> {
    val venmo = systemCategories.categoryByName(SystemCategories.Categories.VENMO)
    val venmoPay = systemCategories.categoryByName(SystemCategories.Categories.VENMO_PAY)

    val result = mutableListOf<DbCategory.Id>()
    if (venmo != null) {
      result.add(venmo.id)
    }
    if (venmoPay != null) {
      result.add(venmoPay.id)
    }

    return result
  }

  companion object {

    /**
     * Venmo posts messages like
     *
     * You paid Tom Smith's $123.45
     *
     * We can look for that notification text and parse the values out
     */
    private val VENMO_WALLET_REGEX =
        "You paid .* ${RAW_STRING_DOLLAR_PRICE}$".toRegex(RegexOption.MULTILINE)
  }
}
