package com.pyamsoft.sleepforbreakfast.spending.guaranteed

import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationInsertDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationQueryDao

internal abstract class BaseGuarantee protected constructor() : SpendingGuarantee {

  private suspend fun upsert(
      insert: NotificationInsertDao,
      notification: DbNotificationWithRegexes
  ) {
    val name = notification.notification.name
    when (val res = insert.insert(notification)) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(res.error) { "Failed to upsert DbNotification $name" }
      }
      is DbInsert.InsertResult.Insert -> {
        Timber.d { "Inserted new DbNotification $name" }
      }
      is DbInsert.InsertResult.Update -> {
        Timber.d { "Updated existing DbNotification $name" }
      }
    }
  }

  protected suspend fun upsertIfUntainted(
      query: NotificationQueryDao,
      insert: NotificationInsertDao,
      notification: DbNotificationWithRegexes,
  ) {
    val name = notification.notification.name
    when (val existing = query.queryById(notification.notification.id)) {
      is Maybe.Data -> {
        val data = existing.data
        if (data.notification.isUntouchedFromSystem) {
          Timber.d { "Upsert existing untouched DbNotification: $name" }
          upsert(insert, notification)
        } else {
          Timber.w { "DbNotification exists but has been user modified, do not upsert. $name" }
        }
      }
      Maybe.None -> {
        Timber.d { "Inserting new DbNotification: $name" }
        upsert(insert, notification)
      }
    }
  }
}
