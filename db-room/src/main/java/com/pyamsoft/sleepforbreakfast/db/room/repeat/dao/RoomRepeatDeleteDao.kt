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
import androidx.room.Delete
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDeleteDao
import com.pyamsoft.sleepforbreakfast.db.room.repeat.entity.RoomDbRepeat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomRepeatDeleteDao : RepeatDeleteDao {

  override suspend fun delete(o: DbRepeat): Boolean =
      withContext(context = Dispatchers.Default) {
        val roomRepeat = RoomDbRepeat.create(o)
        return@withContext daoDelete(roomRepeat) > 0
      }

  @Delete @CheckResult internal abstract fun daoDelete(symbol: RoomDbRepeat): Int
}
