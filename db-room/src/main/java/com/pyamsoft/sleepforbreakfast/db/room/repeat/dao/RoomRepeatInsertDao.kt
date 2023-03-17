/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.sleepforbreakfast.db.room.repeat.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.repeat.entity.RoomDbRepeat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomRepeatInsertDao : RepeatInsertDao {

  @Transaction
  override suspend fun insert(o: DbRepeat): DbInsert.InsertResult<DbRepeat> =
      withContext(context = Dispatchers.IO) {
        val roomRepeat = RoomDbRepeat.create(o)
        return@withContext if (daoQuery(roomRepeat.id) == null) {
          if (daoInsert(roomRepeat) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomRepeat)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomRepeat,
                error = IllegalStateException("Unable to update repeat $roomRepeat"),
            )
          }
        } else {
          if (daoUpdate(roomRepeat) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomRepeat)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomRepeat,
                error = IllegalStateException("Unable to update repeat $roomRepeat"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomDbRepeat): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomDbRepeat.TABLE_NAME} WHERE
        ${RoomDbRepeat.COLUMN_ID} = :id
        LIMIT 1
        """)
  internal abstract suspend fun daoQuery(id: DbRepeat.Id): RoomDbRepeat?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomDbRepeat): Int
}
