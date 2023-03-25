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
import androidx.room.Delete
import com.pyamsoft.sleepforbreakfast.db.room.transaction.entity.RoomDbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDeleteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomTransactionDeleteDao : TransactionDeleteDao {

  override suspend fun delete(o: DbTransaction): Boolean =
      withContext(context = Dispatchers.IO) {
        val roomTransaction = RoomDbTransaction.create(o)
        return@withContext daoDelete(roomTransaction) > 0
      }

  @Delete @CheckResult internal abstract fun daoDelete(symbol: RoomDbTransaction): Int
}
