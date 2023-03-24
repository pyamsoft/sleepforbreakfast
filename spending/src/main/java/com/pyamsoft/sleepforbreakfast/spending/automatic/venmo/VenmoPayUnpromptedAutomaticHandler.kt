package com.pyamsoft.sleepforbreakfast.spending.automatic.venmo

import com.pyamsoft.sleepforbreakfast.core.RAW_STRING_DOLLAR_PRICE
import com.pyamsoft.sleepforbreakfast.spending.automatic.SpendAutomaticHandler
import javax.inject.Inject
import javax.inject.Singleton

/** When you pay someone on Venmo without them requesting you first */
@Singleton
internal class VenmoPayUnpromptedAutomaticHandler @Inject internal constructor() : SpendAutomaticHandler() {

  override fun getRegex(): Regex {
    return VENMO_WALLET_REGEX
  }

  override fun canExtract(packageName: String): Boolean {
    return packageName == "com.venmo"
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
