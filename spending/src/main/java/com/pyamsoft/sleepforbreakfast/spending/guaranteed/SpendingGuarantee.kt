package com.pyamsoft.sleepforbreakfast.spending.guaranteed

import com.pyamsoft.sleepforbreakfast.db.notification.NotificationInsertDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationQueryDao

internal interface SpendingGuarantee {

  suspend fun ensureExistsInDatabase(
      query: NotificationQueryDao,
      insert: NotificationInsertDao,
  )
}
