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

package com.pyamsoft.sleepforbreakfast.db.room.repeat.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import com.pyamsoft.sleepforbreakfast.db.room.repeat.entity.RoomDbRepeat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomRepeatQueryDao : RepeatQueryDao {

  final override suspend fun query(): List<DbRepeat> =
      withContext(context = Dispatchers.IO) { daoQuery() }

  @CheckResult
  @Transaction
  @Query(
      """
      SELECT * FROM ${RoomDbRepeat.TABLE_NAME}
      WHERE ${RoomDbRepeat.COLUMN_ARCHIVED} = FALSE
      """)
  internal abstract suspend fun daoQuery(): List<RoomDbRepeat>

  final override suspend fun queryById(id: DbRepeat.Id): Maybe<out DbRepeat> =
      withContext(context = Dispatchers.IO) {
        when (val transaction = daoQueryById(id)) {
          null -> Maybe.None
          else -> Maybe.Data(transaction)
        }
      }

  @CheckResult
  @Query(
      """
SELECT * FROM ${RoomDbRepeat.TABLE_NAME}
  WHERE ${RoomDbRepeat.COLUMN_ID} = :id
  LIMIT 1
""")
  internal abstract suspend fun daoQueryById(id: DbRepeat.Id): RoomDbRepeat?

  final override suspend fun queryActive(): List<DbRepeat> =
      withContext(context = Dispatchers.IO) { daoQueryActive() }

  @CheckResult
  @Transaction
  @Query(
      """
SELECT * FROM ${RoomDbRepeat.TABLE_NAME}
  WHERE ${RoomDbRepeat.COLUMN_ACTIVE} = TRUE
  AND ${RoomDbRepeat.COLUMN_ARCHIVED} = FALSE
""")
  internal abstract suspend fun daoQueryActive(): List<RoomDbRepeat>
}
