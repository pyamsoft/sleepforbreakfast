/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.spending.guaranteed

import com.pyamsoft.sleepforbreakfast.spending.SpendingApi
import com.pyamsoft.sleepforbreakfast.spending.guaranteed.impl.ChaseBankGuarantee
import com.pyamsoft.sleepforbreakfast.spending.guaranteed.impl.GoogleWalletGuarantee
import com.pyamsoft.sleepforbreakfast.spending.guaranteed.impl.VenmoGuarantee
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class SpendingGuaranteeAppModule {

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindChaseBank(impl: ChaseBankGuarantee): SpendingGuarantee

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindGoogleWallet(impl: GoogleWalletGuarantee): SpendingGuarantee

  @Binds
  @IntoSet
  @SpendingApi
  internal abstract fun bindVenmo(impl: VenmoGuarantee): SpendingGuarantee
}
