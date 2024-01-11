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
        val tainted = data.notification.taintedOn
        if (tainted == null) {
          Timber.d { "Upsert existing untouched DbNotification: $name" }
          upsert(insert, notification)
        } else {
          Timber.w {
            "DbNotification exists but has been user modified, do not upsert. $name $tainted"
          }
        }
      }
      Maybe.None -> {
        Timber.d { "Inserting new DbNotification: $name" }
        upsert(insert, notification)
      }
    }
  }
}
