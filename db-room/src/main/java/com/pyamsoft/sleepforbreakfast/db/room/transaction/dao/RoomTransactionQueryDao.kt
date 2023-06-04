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

package com.pyamsoft.sleepforbreakfast.db.room.transaction.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.pyamsoft.sleepforbreakfast.db.Maybe
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
      withContext(context = Dispatchers.Default) { daoQuery() }

  @CheckResult
  @Transaction
  @Query("""SELECT * FROM ${RoomDbTransaction.TABLE_NAME}""")
  internal abstract suspend fun daoQuery(): List<RoomDbTransaction>

  final override suspend fun queryById(id: DbTransaction.Id): Maybe<out DbTransaction> =
      withContext(context = Dispatchers.Default) {
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

  final override suspend fun queryByRepeat(id: DbRepeat.Id): List<DbTransaction> =
      withContext(context = Dispatchers.Default) { daoQueryByRepeat(id) }

  @CheckResult
  @Transaction
  @Query(
      """
SELECT * FROM ${RoomDbTransaction.TABLE_NAME}
  WHERE ${RoomDbTransaction.COLUMN_REPEAT_ID} = :id
""")
  internal abstract suspend fun daoQueryByRepeat(id: DbRepeat.Id): List<RoomDbTransaction>

  final override suspend fun queryByRepeatOnDates(
      id: DbRepeat.Id,
      dates: Collection<LocalDate>
  ): Set<DbTransaction> =
      withContext(context = Dispatchers.Default) {
        return@withContext daoQueryByRepeatOnDates(id, dates.toList()).toSet()
      }

  @CheckResult
  @Transaction
  @Query(
      """
      SELECT * FROM ${RoomDbTransaction.TABLE_NAME}
        WHERE ${RoomDbTransaction.COLUMN_REPEAT_ID} = :id
        AND ${RoomDbTransaction.COLUMN_REPEAT_DATE} in (:dates)
      """)
  internal abstract suspend fun daoQueryByRepeatOnDates(
      id: DbRepeat.Id,
      dates: List<LocalDate>,
  ): List<RoomDbTransaction>
}
