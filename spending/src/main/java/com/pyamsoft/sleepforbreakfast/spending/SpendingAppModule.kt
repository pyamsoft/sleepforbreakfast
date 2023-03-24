package com.pyamsoft.sleepforbreakfast.spending

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.spending.automatic.AutomaticManager
import com.pyamsoft.sleepforbreakfast.spending.automatic.AutomaticManagerImpl
import com.pyamsoft.sleepforbreakfast.spending.automatic.googlewallet.GoogleWalletAutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.automatic.venmo.VenmoPayRequestedAutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.automatic.venmo.VenmoPayUnpromptedAutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.automatic.venmo.VenmoReceiveRequestedAutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.automatic.venmo.VenmoReceiveUnpromptedAutomaticHandler
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoSet

@Module
abstract class SpendingAppModule {

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

  @Binds
  @CheckResult
  internal abstract fun bindAutomaticManager(impl: AutomaticManagerImpl): AutomaticManager

  @Binds
  @CheckResult
  internal abstract fun bindSpendingHandler(
      impl: SpendingTrackerHandlerImpl
  ): SpendingTrackerHandler
}
