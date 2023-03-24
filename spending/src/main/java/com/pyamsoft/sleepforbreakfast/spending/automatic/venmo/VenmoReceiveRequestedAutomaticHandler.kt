package com.pyamsoft.sleepforbreakfast.spending.automatic.venmo

import com.pyamsoft.sleepforbreakfast.core.RAW_STRING_DOLLAR_PRICE
import com.pyamsoft.sleepforbreakfast.spending.automatic.EarnAutomaticHandler
import javax.inject.Inject
import javax.inject.Singleton

/** When you pay someone on Venmo but they request you to */
@Singleton
internal class VenmoReceiveRequestedAutomaticHandler @Inject internal constructor() : EarnAutomaticHandler() {

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
     * Tom pair you $123.45 - Note about payment here - You now have $250 in your Venmo account
     *
     * We can look for that notification text and parse the values out
     */
    private val VENMO_WALLET_REGEX =
        ".* paid you $RAW_STRING_DOLLAR_PRICE .* You now have".toRegex(RegexOption.MULTILINE)
  }
}
