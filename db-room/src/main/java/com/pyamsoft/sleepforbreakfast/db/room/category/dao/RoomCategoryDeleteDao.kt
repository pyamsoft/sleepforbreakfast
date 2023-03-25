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

package com.pyamsoft.sleepforbreakfast.db.room.category.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Delete
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDeleteDao
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.room.category.entity.RoomDbCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomCategoryDeleteDao : CategoryDeleteDao {

  override suspend fun delete(o: DbCategory): Boolean =
      withContext(context = Dispatchers.IO) {
        val roomCategory = RoomDbCategory.create(o)
        return@withContext daoDelete(roomCategory) > 0
      }

  @Delete @CheckResult internal abstract fun daoDelete(symbol: RoomDbCategory): Int
}
