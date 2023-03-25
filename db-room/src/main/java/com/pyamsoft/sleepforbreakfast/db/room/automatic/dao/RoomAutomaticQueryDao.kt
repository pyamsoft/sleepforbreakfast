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
import androidx.room.Query
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.room.automatic.entity.RoomDbAutomatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomAutomaticQueryDao : AutomaticQueryDao {

  override suspend fun query(): List<DbAutomatic> =
      withContext(context = Dispatchers.IO) { daoQuery() }

  @CheckResult
  @Query("""SELECT * FROM ${RoomDbAutomatic.TABLE_NAME}""")
  internal abstract suspend fun daoQuery(): List<RoomDbAutomatic>

  override suspend fun queryByNotification(
      notificationId: Int,
      notificationKey: String,
      notificationGroup: String,
      notificationPackageName: String,
      notificationPostTime: Long
  ): Maybe<out DbAutomatic> =
      withContext(context = Dispatchers.IO) {
        when (val result =
            daoQueryByNotification(
                id = notificationId,
                key = notificationKey,
                group = notificationGroup,
                packageName = notificationPackageName,
                postTime = notificationPostTime,
            )) {
          null -> Maybe.None
          else -> Maybe.Data(result)
        }
      }

  @CheckResult
  @Query(
      """
SELECT * FROM ${RoomDbAutomatic.TABLE_NAME}
  WHERE ${RoomDbAutomatic.COLUMN_NOTIFICATION_ID} = :id
  AND ${RoomDbAutomatic.COLUMN_NOTIFICATION_KEY} = :key
  AND ${RoomDbAutomatic.COLUMN_NOTIFICATION_GROUP} = :group
  AND ${RoomDbAutomatic.COLUMN_NOTIFICATION_PACKAGE} = :packageName
  AND ${RoomDbAutomatic.COLUMN_NOTIFICATION_POST_TIME} = :postTime
  LIMIT 1
""")
  internal abstract suspend fun daoQueryByNotification(
      id: Int,
      key: String,
      group: String,
      packageName: String,
      postTime: Long
  ): RoomDbAutomatic?

  override suspend fun queryUnused(): List<DbAutomatic> =
      withContext(context = Dispatchers.IO) { daoQueryUnused() }

  @CheckResult
  @Query(
      """
SELECT * FROM ${RoomDbAutomatic.TABLE_NAME} WHERE NOT ${RoomDbAutomatic.COLUMN_USED}
""")
  internal abstract suspend fun daoQueryUnused(): List<RoomDbAutomatic>
}
