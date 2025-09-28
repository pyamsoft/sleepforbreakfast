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

package com.pyamsoft.sleepforbreakfast.db.room.automatic.dao

import androidx.annotation.CheckResult
import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.room.automatic.entity.RoomDbAutomatic
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Dao
internal abstract class RoomAutomaticQueryDao : AutomaticQueryDao {

  final override suspend fun query(): List<DbAutomatic> =
      withContext(context = Dispatchers.Default) { daoQuery() }

  @CheckResult
  @Transaction
  @Query("""SELECT * FROM ${RoomDbAutomatic.TABLE_NAME}""")
  internal abstract suspend fun daoQuery(): List<RoomDbAutomatic>

  final override suspend fun queryByNotification(
      notificationId: Int,
      notificationKey: String,
      notificationGroup: String,
      notificationPackageName: String,
      notificationMatchText: String,
  ): Maybe<out DbAutomatic> =
      withContext(context = Dispatchers.Default) {
        when (
            val result =
                daoQueryByNotification(
                    id = notificationId,
                    key = notificationKey,
                    group = notificationGroup,
                    packageName = notificationPackageName,
                    matchText = notificationMatchText,
                )
        ) {
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
  AND ${RoomDbAutomatic.COLUMN_NOTIFICATION_MATCH_TEXT} = :matchText
  LIMIT 1
"""
  )
  internal abstract suspend fun daoQueryByNotification(
      id: Int,
      key: String,
      group: String,
      packageName: String,
      matchText: String,
  ): RoomDbAutomatic?

  final override suspend fun queryUnused(): List<DbAutomatic> =
      withContext(context = Dispatchers.Default) { daoQueryUnused() }

  @CheckResult
  @Transaction
  @Query(
      """
SELECT * FROM ${RoomDbAutomatic.TABLE_NAME} WHERE NOT ${RoomDbAutomatic.COLUMN_USED}
"""
  )
  internal abstract suspend fun daoQueryUnused(): List<RoomDbAutomatic>

  final override suspend fun queryById(id: DbAutomatic.Id): Maybe<out DbAutomatic> =
      withContext(context = Dispatchers.Default) {
        when (val transaction = daoQueryById(id)) {
          null -> Maybe.None
          else -> Maybe.Data(transaction)
        }
      }

  @CheckResult
  @Query(
      """
SELECT * FROM ${RoomDbAutomatic.TABLE_NAME}
  WHERE ${RoomDbAutomatic.COLUMN_ID} = :id
  LIMIT 1
"""
  )
  internal abstract suspend fun daoQueryById(id: DbAutomatic.Id): RoomDbAutomatic?
}
