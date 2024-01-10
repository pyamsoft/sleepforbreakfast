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

package com.pyamsoft.sleepforbreakfast.db.room.notification.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotification
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationQueryDao
import com.pyamsoft.sleepforbreakfast.db.room.notification.entity.RoomDbNotification
import com.pyamsoft.sleepforbreakfast.db.room.notification.entity.RoomDbNotificationWithRegexes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomNotificationQueryDao : NotificationQueryDao {

  final override suspend fun query(): List<DbNotificationWithRegexes> =
      withContext(context = Dispatchers.Default) { daoQuery() }

  @CheckResult
  @Transaction
  @Query("""
      SELECT * FROM ${RoomDbNotification.TABLE_NAME}
      """)
  internal abstract suspend fun daoQuery(): List<RoomDbNotificationWithRegexes>

  final override suspend fun queryById(
      id: DbNotification.Id
  ): Maybe<out DbNotificationWithRegexes> =
      withContext(context = Dispatchers.Default) {
        when (val transaction = daoQueryById(id)) {
          null -> Maybe.None
          else -> Maybe.Data(transaction)
        }
      }

  @Transaction
  @CheckResult
  @Query(
      """
SELECT * FROM ${RoomDbNotification.TABLE_NAME}
  WHERE ${RoomDbNotification.COLUMN_ID} = :id
  LIMIT 1
""")
  internal abstract suspend fun daoQueryById(id: DbNotification.Id): RoomDbNotificationWithRegexes?
}
