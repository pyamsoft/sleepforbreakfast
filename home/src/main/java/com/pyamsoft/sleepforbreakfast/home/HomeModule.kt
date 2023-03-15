package com.pyamsoft.sleepforbreakfast.home

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.home.notification.NotificationListenerStatus
import com.pyamsoft.sleepforbreakfast.home.notification.NotificationListenerStatusImpl
import dagger.Binds
import dagger.Module

@Module
abstract class HomeModule {

  @Binds
  @CheckResult
  internal abstract fun bindNotificationListenerStatus(
      impl: NotificationListenerStatusImpl
  ): NotificationListenerStatus
}
