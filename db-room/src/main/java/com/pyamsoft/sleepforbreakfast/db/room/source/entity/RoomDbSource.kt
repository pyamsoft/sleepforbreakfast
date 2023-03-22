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

package com.pyamsoft.sleepforbreakfast.db.room.source.entity

import androidx.annotation.CheckResult
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import java.time.LocalDateTime

@Entity(tableName = RoomDbSource.TABLE_NAME)
internal data class RoomDbSource
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbSource.Id,
    @JvmField @ColumnInfo(name = COLUMN_CREATED_AT) val dbCreatedAt: LocalDateTime,
    @JvmField @ColumnInfo(name = COLUMN_NAME) val dbName: String,
    @JvmField @ColumnInfo(name = COLUMN_NOTE, defaultValue = "") val dbNote: String,
    @JvmField @ColumnInfo(name = COLUMN_ACCOUNT, defaultValue = "") val dbAccount: String,
) : DbSource {

  @Ignore override val id: DbSource.Id = dbId

  @Ignore override val createdAt = dbCreatedAt

  @Ignore override val name: String = dbName

  @Ignore override val note = dbNote

  @Ignore override val accountNumber = dbAccount

  @Ignore
  override fun name(name: String): DbSource {
    return this.copy(dbName = name)
  }

  @Ignore
  override fun note(note: String): DbSource {
    return this.copy(dbNote = note)
  }

  @Ignore
  override fun accountNumber(accountNumber: String): DbSource {
    return this.copy(dbAccount = accountNumber)
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_sources_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_CREATED_AT = "created_at"

    @Ignore internal const val COLUMN_NAME = "name"

    @Ignore internal const val COLUMN_NOTE = "note"

    @Ignore internal const val COLUMN_ACCOUNT = "account"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbSource): RoomDbSource {
      return if (item is RoomDbSource) item
      else {
        RoomDbSource(
            item.id,
            item.createdAt,
            item.name,
            item.note,
            item.accountNumber,
        )
      }
    }
  }
}
