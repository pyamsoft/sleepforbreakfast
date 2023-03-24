package com.pyamsoft.sleepforbreakfast.spending

import android.os.Bundle
import androidx.annotation.CheckResult

internal interface AutomaticHandler {

  @CheckResult fun extract(bundle: Bundle): PaymentNotification?

  @CheckResult fun canExtract(packageName: String): Boolean
}
