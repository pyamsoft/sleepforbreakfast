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
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_COUNT_UPDATE_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_ID_INSERT_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.transaction.entity.RoomDbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomTransactionInsertDao : TransactionInsertDao {

  override suspend fun insert(o: DbTransaction): DbInsert.InsertResult<DbTransaction> =
      withContext(context = Dispatchers.IO) {
        val roomTransaction = RoomDbTransaction.create(o)
        return@withContext if (daoQuery(roomTransaction.id) == null) {
          if (daoInsert(roomTransaction) != ROOM_ROW_ID_INSERT_INVALID) {
            DbInsert.InsertResult.Insert(roomTransaction)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomTransaction,
                error = IllegalStateException("Unable to update transaction $roomTransaction"),
            )
          }
        } else {
          if (daoUpdate(roomTransaction) > ROOM_ROW_COUNT_UPDATE_INVALID) {
            DbInsert.InsertResult.Update(roomTransaction)
          } else {
            DbInsert.InsertResult.Fail(
                data = roomTransaction,
                error = IllegalStateException("Unable to update transaction $roomTransaction"),
            )
          }
        }
      }

  @Insert(onConflict = OnConflictStrategy.ABORT)
  internal abstract suspend fun daoInsert(symbol: RoomDbTransaction): Long

  @CheckResult
  @Query(
      """
        SELECT * FROM ${RoomDbTransaction.TABLE_NAME} WHERE
        ${RoomDbTransaction.COLUMN_ID} = :id
        LIMIT 1
        """)
  internal abstract suspend fun daoQuery(id: DbTransaction.Id): RoomDbTransaction?

  @Update internal abstract suspend fun daoUpdate(symbol: RoomDbTransaction): Int
}
