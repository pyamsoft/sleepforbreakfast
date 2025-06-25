/*
 * Copyright 2025 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.db.room.notification.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Transaction
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationDeleteDao
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_COUNT_DELETE_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.notification.entity.RoomDbNotification
import com.pyamsoft.sleepforbreakfast.db.room.notification.entity.RoomDbNotificationMatchRegex
import com.pyamsoft.sleepforbreakfast.db.room.notification.entity.RoomDbNotificationWithRegexes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomNotificationDeleteDao : NotificationDeleteDao {

  // Transaction methods cannot be final
  @Transaction
  /* final */ override suspend fun delete(o: DbNotificationWithRegexes): Boolean =
      withContext(context = Dispatchers.Default) {
        val roomNotification = RoomDbNotificationWithRegexes.create(o)
        return@withContext if (daoDeleteNotification(roomNotification.notification) >
            ROOM_ROW_COUNT_DELETE_INVALID) {
          daoDeleteMatchRegexes(roomNotification.matchRegexes)
          true
        } else {
          false
        }
      }

  @Delete @CheckResult internal abstract fun daoDeleteNotification(symbol: RoomDbNotification): Int

  @Delete internal abstract fun daoDeleteMatchRegexes(symbols: List<RoomDbNotificationMatchRegex>)
}
