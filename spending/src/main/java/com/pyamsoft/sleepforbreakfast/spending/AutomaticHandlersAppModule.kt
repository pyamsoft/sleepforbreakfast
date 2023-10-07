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

import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.ChaseBankEmailAutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.GoogleWalletAutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.VenmoPayRequestedAutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.VenmoPayUnpromptedAutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.VenmoReceiveRequestedAutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.automatic.handlers.VenmoReceiveUnpromptedAutomaticHandler
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class AutomaticHandlersAppModule {

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindChaseEmailHandler(
      impl: ChaseBankEmailAutomaticHandler
  ): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindGoogleWalletHandler(
      impl: GoogleWalletAutomaticHandler
  ): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmoPayUnprompted(
      impl: VenmoPayUnpromptedAutomaticHandler
  ): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmoPayRequested(
      impl: VenmoPayRequestedAutomaticHandler
  ): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmoReceiveUnprompted(
      impl: VenmoReceiveRequestedAutomaticHandler
  ): AutomaticHandler

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmoReceiveRequested(
      impl: VenmoReceiveUnpromptedAutomaticHandler
  ): AutomaticHandler
}
