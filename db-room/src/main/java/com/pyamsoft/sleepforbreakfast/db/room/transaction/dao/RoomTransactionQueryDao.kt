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

package com.pyamsoft.sleepforbreakfast.db.room.transaction.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.room.transaction.entity.RoomDbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import java.time.LocalDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomTransactionQueryDao : TransactionQueryDao {

  final override suspend fun query(): List<DbTransaction> =
      withContext(context = Dispatchers.IO) { daoQuery() }

  @CheckResult
  @Query("""SELECT * FROM ${RoomDbTransaction.TABLE_NAME}""")
  internal abstract suspend fun daoQuery(): List<RoomDbTransaction>

  final override suspend fun queryById(id: DbTransaction.Id): Maybe<out DbTransaction> =
      withContext(context = Dispatchers.IO) {
        when (val transaction = daoQueryById(id)) {
          null -> Maybe.None
          else -> Maybe.Data(transaction)
        }
      }

  @CheckResult
  @Query(
      """
SELECT * FROM ${RoomDbTransaction.TABLE_NAME}
  WHERE ${RoomDbTransaction.COLUMN_ID} = :id
  LIMIT 1
""")
  internal abstract suspend fun daoQueryById(id: DbTransaction.Id): RoomDbTransaction?

  final override suspend fun queryByRepeatOnDate(
      id: DbRepeat.Id,
      date: LocalDate
  ): Maybe<out DbTransaction> {
    TODO("Not yet implemented")
  }

  @CheckResult
  @Query(
      """
SELECT * FROM ${RoomDbTransaction.TABLE_NAME}
  WHERE ${RoomDbTransaction.COLUMN_REPEAT_ID} = :id
  AND ${RoomDbTransaction.COLUMN_CREATED_AT} LIKE :dateString
  LIMIT 1
""")
  internal abstract suspend fun daoQueryByRepeatOnDate(
      id: DbRepeat.Id,
      dateString: String
  ): RoomDbTransaction?
}
