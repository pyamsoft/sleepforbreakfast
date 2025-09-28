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

package com.pyamsoft.sleepforbreakfast.db.room.category.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.category.CategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.category.entity.RoomDbCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomCategoryInsertDao : CategoryInsertDao {

  // Transaction methods cannot be final
  @Transaction
  /* final */ override suspend fun insert(o: DbCategory): DbInsert.InsertResult<DbCategory> =
      withContext(context = Dispatchers.Default) {
        val roomCategory = RoomDbCategory.create(o)
        return@withContext if (daoQuery(roomCategory.id) == null) {
          if (daoInsert(roomCategory) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomCategory)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomCategory,
                error = IllegalStateException("Unable to update category $roomCategory"),
            )
          }
        } else {
          if (daoUpdate(roomCategory) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomCategory)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomCategory,
                error = IllegalStateException("Unable to update category $roomCategory"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomDbCategory): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomDbCategory.TABLE_NAME} WHERE
        ${RoomDbCategory.COLUMN_ID} = :id
        LIMIT 1
        """
  )
  internal abstract suspend fun daoQuery(id: DbCategory.Id): RoomDbCategory?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomDbCategory): Int
}
