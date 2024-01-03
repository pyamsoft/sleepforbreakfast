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

package com.pyamsoft.sleepforbreakfast.spending

import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.ChaseBankSpend
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.ChaseBankEarn
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.GoogleWalletSpend
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.VenmoEarn
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.VenmoSpend
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class AutomaticHandlersAppModule {

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindChaseEmailHandler(impl: ChaseBankSpend): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindGoogleWalletHandler(impl: GoogleWalletSpend): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmoPayRequested(impl: VenmoSpend): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmoReceiveUnprompted(impl: VenmoEarn): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindChaseBankEarnEmail(impl: ChaseBankEarn): AutomaticHandler
}
