package com.pyamsoft.sleepforbreakfast.spending.automatic

import android.os.Bundle
import com.pyamsoft.sleepforbreakfast.spending.AutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.PaymentNotification
import com.pyamsoft.sleepforbreakfast.spending.SpendingApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AutomaticManagerImpl
@Inject
internal constructor(
    // Need to use MutableSet instead of Set because of Java -> Kotlin fun.
    @SpendingApi private val handlers: MutableSet<AutomaticHandler>,
) : AutomaticManager {

  override fun extractPayment(
      packageName: String,
      bundle: Bundle,
  ): PaymentNotification? {
    // This loop continues until we find a result since multiple handlers may
    // handle the same packagename, like Venmo
    for (handler in handlers) {
      if (handler.canExtract(packageName)) {
        val result = handler.extract(bundle)
        if (result != null) {
          return result
        }
      }
    }

    return null
  }
}
