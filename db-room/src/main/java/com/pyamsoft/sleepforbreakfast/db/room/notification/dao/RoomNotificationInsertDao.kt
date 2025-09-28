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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotification
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationInsertDao
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.notification.entity.RoomDbNotification
import com.pyamsoft.sleepforbreakfast.db.room.notification.entity.RoomDbNotificationMatchRegex
import com.pyamsoft.sleepforbreakfast.db.room.notification.entity.RoomDbNotificationWithRegexes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomNotificationInsertDao : NotificationInsertDao {

  // Transaction methods cannot be final
  @Transaction
  /* final */ override suspend fun insert(
      o: DbNotificationWithRegexes
  ): DbInsert.InsertResult<DbNotificationWithRegexes> =
      withContext(context = Dispatchers.Default) {
        val roomNotification = RoomDbNotificationWithRegexes.create(o)
        val existing = daoQuery(roomNotification.notification.id)
        return@withContext if (existing == null) {
          if (daoInsert(roomNotification.notification) != ROOM_ROW_ID_INSERT_INVALID) {
            if (daoInsert(roomNotification.matchRegexes).all { it != ROOM_ROW_ID_INSERT_INVALID }) {
              DbInsert.InsertResult.Insert(roomNotification)
            } else {
              DbInsert.InsertResult.Fail(
                  data = roomNotification,
                  error = IllegalStateException("Unable to update category $roomNotification"),
              )
            }
          } else {
            DbInsert.InsertResult.Fail(
                data = roomNotification,
                error = IllegalStateException("Unable to update category $roomNotification"),
            )
          }
        } else {
          if (daoUpdate(roomNotification.notification) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            // Insert any new regexes
            val newRegexes =
                roomNotification.matchRegexes.filter { r ->
                  val found = existing.dbMatchRegexes.firstOrNull { it.id == r.id }
                  return@filter found == null
                }

            if (newRegexes.isNotEmpty()) {
              if (daoInsert(newRegexes).all { it == ROOM_ROW_ID_INSERT_INVALID }) {
                Timber.w {
                  "Failed to insert match regexes for notification ${roomNotification.notification}"
                }
                DbInsert.InsertResult.Fail(
                    data = roomNotification,
                    error = IllegalStateException("Unable to update category $roomNotification"),
                )
              }
            }

            // Update existing regexes
            if (daoUpdate(roomNotification.matchRegexes) > ROOM_ROW_COUNT_UPDATE_INVALID) {
              DbInsert.InsertResult.Update(roomNotification)
            } else {
              DbInsert.InsertResult.Fail(
                  data = roomNotification,
                  error = IllegalStateException("Unable to update category $roomNotification"),
              )
            }
          } else {
            DbInsert.InsertResult.Fail(
                data = roomNotification,
                error = IllegalStateException("Unable to update category $roomNotification"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomDbNotification): Long

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbols: List<RoomDbNotificationMatchRegex>): Array<Long>

  @Transaction
  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomDbNotification.TABLE_NAME} WHERE
        ${RoomDbNotification.COLUMN_ID} = :id
        LIMIT 1
        """
  )
  internal abstract suspend fun daoQuery(id: DbNotification.Id): RoomDbNotificationWithRegexes?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomDbNotification): Int

  @Update internal abstract suspend fun daoUpdate(symbols: List<RoomDbNotificationMatchRegex>): Int
}
