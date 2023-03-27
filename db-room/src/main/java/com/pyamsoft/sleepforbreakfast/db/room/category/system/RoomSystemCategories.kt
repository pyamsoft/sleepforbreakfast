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

package com.pyamsoft.sleepforbreakfast.db.room.category.system

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.category.system.RequiredCategories
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.category.entity.RoomDbCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Dao
internal abstract class RoomSystemCategories internal constructor() {

  /** Needs to be open for Room Transaction */
  @Transaction
  open suspend fun categoryByName(
      category: RequiredCategories,
      make: (RequiredCategories) -> RoomDbCategory,
  ): DbCategory? =
      withContext(context = Dispatchers.IO) {
        when (val existing = daoQueryBySystemName(category.displayName)) {
          null -> {
            val db = make(category)
            when (daoInsert(db)) {
              ROOM_ROW_ID_INSERT_INVALID -> {
                Timber.w("Failed inserted new system category: $category")
                return@withContext null
              }
              else -> {
                Timber.d("Inserted new system category: $category")
                return@withContext db
              }
            }
          }
          else -> {
            Timber.d("System category already exists: $existing")
            return@withContext existing
          }
        }
      }

  @CheckResult
  @Query(
      """
SELECT * FROM ${RoomDbCategory.TABLE_NAME}
  WHERE ${RoomDbCategory.COLUMN_NAME} = :name
  AND ${RoomDbCategory.COLUMN_SYSTEM} = TRUE
  LIMIT 1
""")
  internal abstract suspend fun daoQueryBySystemName(name: String): RoomDbCategory?

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(db: RoomDbCategory): Long
}
