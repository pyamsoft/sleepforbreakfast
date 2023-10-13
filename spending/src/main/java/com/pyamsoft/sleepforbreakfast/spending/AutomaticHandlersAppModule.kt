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

import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.ChaseBankAppSpend
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.ChaseBankEmailEarn
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.GoogleWalletSpend
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.VenmoReceiveRequested
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.VenmoReceiveUnprompted
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.VenmoSpendRequested
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.VenmoSpendUnprompted
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class AutomaticHandlersAppModule {

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindChaseEmailHandler(impl: ChaseBankAppSpend): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindGoogleWalletHandler(impl: GoogleWalletSpend): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmoPayUnprompted(impl: VenmoSpendUnprompted): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmoPayRequested(impl: VenmoSpendRequested): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmoReceiveUnprompted(impl: VenmoReceiveRequested): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmoReceiveRequested(impl: VenmoReceiveUnprompted): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindChaseBankEarnEmail(impl: ChaseBankEmailEarn): AutomaticHandler
}
