package com.pyamsoft.sleepforbreakfast.spending

import android.app.Notification
import android.os.Bundle
import android.service.notification.StatusBarNotification

interface SpendingTrackerHandler {

  suspend fun processNotification(
      sbn: StatusBarNotification,
      extras: Bundle,
  )
}
