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

package com.pyamsoft.sleepforbreakfast.db.room.category.entity

import androidx.annotation.CheckResult
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import java.time.LocalDateTime

// Kotlin 2.0.20, Room 2.6.1 - Copy annotations currently break Room KSP. Do not use.
@Entity(tableName = RoomDbCategory.TABLE_NAME)
internal data class RoomDbCategory
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbCategory.Id,
    @JvmField @ColumnInfo(name = COLUMN_CREATED_AT) val dbCreatedAt: LocalDateTime,
    @JvmField @ColumnInfo(name = COLUMN_NAME) val dbName: String,
    @JvmField @ColumnInfo(name = COLUMN_NOTE) val dbNote: String,
    @JvmField @ColumnInfo(name = COLUMN_SYSTEM) val dbSystem: Boolean,
    @JvmField @ColumnInfo(name = COLUMN_ACTIVE) val dbActive: Boolean,
    @JvmField @ColumnInfo(name = COLUMN_ARCHIVED) val dbArchived: Boolean,
    @JvmField
    @ColumnInfo(
        name = COLUMN_COLOR,
        // TODO remove for first release and kill migrations
        defaultValue = "0",
    )
    val dbColor: Long,
) : DbCategory {

  @Ignore override val id: DbCategory.Id = dbId

  @Ignore override val createdAt = dbCreatedAt

  @Ignore override val name: String = dbName

  @Ignore override val note = dbNote

  @Ignore override val system = dbSystem

  @Ignore override val active = dbActive

  @Ignore override val archived = dbArchived

  @Ignore override val color = dbColor

  @Ignore
  override fun name(name: String): DbCategory {
    return this.copy(dbName = name)
  }

  @Ignore
  override fun note(note: String): DbCategory {
    return this.copy(dbNote = note)
  }

  @Ignore
  override fun systemLevel(): DbCategory {
    return this.copy(dbSystem = true)
  }

  @Ignore
  override fun activate(): DbCategory {
    return this.copy(dbActive = true)
  }

  @Ignore
  override fun deactivate(): DbCategory {
    return this.copy(dbActive = false)
  }

  @Ignore
  override fun archive(): DbCategory {
    return this.copy(dbArchived = true)
  }

  @Ignore
  override fun unarchive(): DbCategory {
    return this.copy(dbArchived = false)
  }

  @Ignore
  override fun color(color: Long): DbCategory {
    return this.copy(dbColor = color)
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_category_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_CREATED_AT = "created_at"

    @Ignore internal const val COLUMN_NAME = "name"

    @Ignore internal const val COLUMN_NOTE = "note"

    @Ignore internal const val COLUMN_SYSTEM = "system"

    @Ignore internal const val COLUMN_ACTIVE = "active"

    @Ignore internal const val COLUMN_ARCHIVED = "archived"

    @Ignore internal const val COLUMN_COLOR = "color"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbCategory): RoomDbCategory {
      return if (item is RoomDbCategory) item
      else {
        RoomDbCategory(
            item.id,
            item.createdAt,
            item.name,
            item.note,
            item.system,
            item.active,
            item.archived,
            item.color,
        )
      }
    }
  }
}
