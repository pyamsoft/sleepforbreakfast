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

package com.pyamsoft.sleepforbreakfast.db.room.automatic.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Transaction
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticDeleteDao
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.room.ROOM_ROW_COUNT_DELETE_INVALID
import com.pyamsoft.sleepforbreakfast.db.room.automatic.entity.RoomDbAutomatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomAutomaticDeleteDao : AutomaticDeleteDao {

  final override suspend fun delete(o: DbAutomatic): Boolean =
      withContext(context = Dispatchers.Default) {
        val roomAutomatic = RoomDbAutomatic.create(o)
        return@withContext daoDelete(roomAutomatic) > ROOM_ROW_COUNT_DELETE_INVALID
      }

  @Delete @CheckResult internal abstract fun daoDelete(symbol: RoomDbAutomatic): Int
}
