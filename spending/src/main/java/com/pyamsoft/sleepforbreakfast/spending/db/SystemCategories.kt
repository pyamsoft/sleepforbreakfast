package com.pyamsoft.sleepforbreakfast.spending.db

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory

internal interface SystemCategories {

  @CheckResult suspend fun categoryByName(category: Categories): DbCategory?

  enum class Categories(val displayName: String) {
    VENMO("Venmo"),
    VENMO_PAY("Venmo Payments"),
    VENMO_REQUESTS("Venmo Requests"),
    GOOGLE_WALLET("Google Wallet"),
  }
}
