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
