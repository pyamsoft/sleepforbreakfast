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

package com.pyamsoft.sleepforbreakfast.automatic

import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotification
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationChangeEvent
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationDeleteDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationInsertDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationQueryDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationRealtime
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractorImpl
import com.pyamsoft.sleepforbreakfast.spending.guaranteed.GuaranteedSpending
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class AutomaticInteractorImpl
@Inject
constructor(
    private val automaticInsertDao: NotificationInsertDao,
    private val automaticDeleteDao: NotificationDeleteDao,
    private val automaticRealtime: NotificationRealtime,
    private val automaticQueryDao: NotificationQueryDao,
    private val automaticQueryCache: NotificationQueryDao.Cache,
    private val guaranteedSpending: GuaranteedSpending,
) :
    AutomaticInteractor,
    ListInteractorImpl<DbNotification.Id, DbNotificationWithRegexes, NotificationChangeEvent>() {

  override suspend fun performQueryAll(): List<DbNotificationWithRegexes> {
    guaranteedSpending.ensureExistsInDatabase()
    return automaticQueryDao.query()
  }

  override suspend fun performQueryOne(
      id: DbNotification.Id
  ): Maybe<out DbNotificationWithRegexes> {
    return automaticQueryDao.queryById(id)
  }

  override suspend fun performClearCache() {
    automaticQueryCache.invalidate()
  }

  override suspend fun performClearCache(id: DbNotification.Id) {
    automaticQueryCache.invalidateById(id)
  }

  override fun listenForItemChanges(): Flow<NotificationChangeEvent> {
    return automaticRealtime.listenForChanges()
  }

  override suspend fun performInsert(
      item: DbNotificationWithRegexes
  ): DbInsert.InsertResult<DbNotificationWithRegexes> {
    return automaticInsertDao.insert(item)
  }

  override suspend fun performDelete(item: DbNotificationWithRegexes): Boolean {
    return automaticDeleteDao.delete(item)
  }
}
