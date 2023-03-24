package com.pyamsoft.sleepforbreakfast.spending.automatic

import android.os.Bundle
import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.spending.PaymentNotification

internal interface AutomaticManager {

  @CheckResult
  fun extractPayment(
      packageName: String,
      bundle: Bundle,
  ): PaymentNotification?
}
