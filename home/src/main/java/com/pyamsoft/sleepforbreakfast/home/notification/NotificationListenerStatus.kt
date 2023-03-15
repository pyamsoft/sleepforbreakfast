package com.pyamsoft.sleepforbreakfast.home.notification

import androidx.annotation.CheckResult
import kotlinx.coroutines.flow.Flow

interface NotificationListenerStatus {

  @CheckResult fun isNotificationListenerActive(): Flow<Boolean>

  @CheckResult suspend fun activateNotificationListener()
}
