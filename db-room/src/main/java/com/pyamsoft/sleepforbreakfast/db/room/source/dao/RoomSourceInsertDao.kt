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

package com.pyamsoft.sleepforbreakfast.db.room.source.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.source.entity.RoomDbSource
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.source.SourceInsertDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomSourceInsertDao : SourceInsertDao {

  @Transaction
  override suspend fun insert(o: DbSource): DbInsert.InsertResult<DbSource> =
      withContext(context = Dispatchers.IO) {
        val roomSource = RoomDbSource.create(o)
        return@withContext if (daoQuery(roomSource.id) == null) {
          if (daoInsert(roomSource) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomSource)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomSource,
                error = IllegalStateException("Unable to update source $roomSource"),
            )
          }
        } else {
          if (daoUpdate(roomSource) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomSource)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomSource,
                error = IllegalStateException("Unable to update source $roomSource"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomDbSource): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomDbSource.TABLE_NAME} WHERE
        ${RoomDbSource.COLUMN_ID} = :id
        LIMIT 1
        """)
  internal abstract suspend fun daoQuery(id: DbSource.Id): RoomDbSource?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomDbSource): Int
}
