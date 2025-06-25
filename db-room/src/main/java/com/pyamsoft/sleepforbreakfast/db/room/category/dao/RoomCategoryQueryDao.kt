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
import androidx.room.Query
import androidx.room.Transaction
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.room.category.entity.RoomDbCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomCategoryQueryDao : CategoryQueryDao {

  final override suspend fun query(): List<DbCategory> =
      withContext(context = Dispatchers.Default) { daoQuery() }

  @CheckResult
  @Transaction
  @Query(
      """
      SELECT * FROM ${RoomDbCategory.TABLE_NAME}
      WHERE ${RoomDbCategory.COLUMN_ARCHIVED} = 0
      """)
  internal abstract suspend fun daoQuery(): List<RoomDbCategory>

  final override suspend fun queryById(id: DbCategory.Id): Maybe<out DbCategory> =
      withContext(context = Dispatchers.Default) {
        when (val transaction = daoQueryById(id)) {
          null -> Maybe.None
          else -> Maybe.Data(transaction)
        }
      }

  @CheckResult
  @Query(
      """
SELECT * FROM ${RoomDbCategory.TABLE_NAME}
  WHERE ${RoomDbCategory.COLUMN_ID} = :id
  LIMIT 1
""")
  internal abstract suspend fun daoQueryById(id: DbCategory.Id): RoomDbCategory?
}
