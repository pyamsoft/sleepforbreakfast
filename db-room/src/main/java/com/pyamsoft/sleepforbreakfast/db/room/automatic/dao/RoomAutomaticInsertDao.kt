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

package com.pyamsoft.sleepforbreakfast.db.room.automatic.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.automatic.entity.RoomDbAutomatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomAutomaticInsertDao : AutomaticInsertDao {

  // Transaction methods cannot be final
  @Transaction
  override suspend fun insert(o: DbAutomatic): DbInsert.InsertResult<DbAutomatic> =
      withContext(context = Dispatchers.Default) {
        val roomAutomatic = RoomDbAutomatic.create(o)
        return@withContext if (daoQuery(roomAutomatic.id) == null) {
          if (daoInsert(roomAutomatic) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomAutomatic)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomAutomatic,
                error = IllegalStateException("Unable to update automatic $roomAutomatic"),
            )
          }
        } else {
          if (daoUpdate(roomAutomatic) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomAutomatic)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomAutomatic,
                error = IllegalStateException("Unable to update automatic $roomAutomatic"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomDbAutomatic): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomDbAutomatic.TABLE_NAME} WHERE
        ${RoomDbAutomatic.COLUMN_ID} = :id
        LIMIT 1
        """)
  internal abstract suspend fun daoQuery(id: DbAutomatic.Id): RoomDbAutomatic?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomDbAutomatic): Int
}
