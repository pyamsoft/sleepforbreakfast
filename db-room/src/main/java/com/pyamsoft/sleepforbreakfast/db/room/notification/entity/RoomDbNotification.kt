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

package com.pyamsoft.sleepforbreakfast.db.room.notification.entity

import androidx.annotation.CheckResult
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotification
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import java.time.LocalDateTime

@ConsistentCopyVisibility
@Entity(tableName = RoomDbNotification.TABLE_NAME)
internal data class RoomDbNotification
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbNotification.Id,
    @JvmField @ColumnInfo(name = COLUMN_CREATED_AT) val dbCreatedAt: LocalDateTime,
    @JvmField @ColumnInfo(name = COLUMN_NAME) val dbName: String,
    @JvmField @ColumnInfo(name = COLUMN_ENABLED) val dbEnabled: Boolean,
    @JvmField @ColumnInfo(name = COLUMN_TYPE) val dbType: DbTransaction.Type,
    @JvmField @ColumnInfo(name = COLUMN_SYSTEM) val dbSystem: Boolean,
    @JvmField @ColumnInfo(name = COLUMN_WATCH_PACKAGES) val dbWatchPackages: List<String>,
    @JvmField @ColumnInfo(name = COLUMN_IS_UNTOUCHED_SYSTEM) val dbTaintedOn: LocalDateTime?,
) : DbNotification {

  @Ignore override val id = dbId

  @Ignore override val createdAt = dbCreatedAt

  @Ignore override val name = dbName

  @Ignore override val enabled = dbEnabled

  @Ignore override val system = dbSystem

  @Ignore override val type = dbType

  @Ignore override val actOnPackageNames = dbWatchPackages

  @Ignore override val taintedOn = dbTaintedOn

  @Ignore
  override fun name(name: String): DbNotification {
    return this.copy(dbName = name)
  }

  @Ignore
  override fun enabled(enabled: Boolean): DbNotification {
    return this.copy(dbEnabled = enabled)
  }

  @Ignore
  override fun actOnPackageName(packageNames: Collection<String>): DbNotification {
    return this.copy(
        dbWatchPackages = packageNames.toList(),
    )
  }

  @Ignore
  override fun type(type: DbTransaction.Type): DbNotification {
    return this.copy(dbType = type)
  }

  @Ignore
  override fun markTaintedByUser(date: LocalDateTime): DbNotification {
    return if (dbTaintedOn == null) this.copy(dbTaintedOn = date) else this
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_notification_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_CREATED_AT = "created_at"

    @Ignore internal const val COLUMN_NAME = "name"

    @Ignore internal const val COLUMN_ENABLED = "note"

    @Ignore internal const val COLUMN_TYPE = "type"

    @Ignore internal const val COLUMN_SYSTEM = "system"

    @Ignore internal const val COLUMN_IS_UNTOUCHED_SYSTEM = "tainted_on"

    @Ignore internal const val COLUMN_WATCH_PACKAGES = "watch_packages"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbNotification): RoomDbNotification {
      return if (item is RoomDbNotification) item
      else {
        RoomDbNotification(
            item.id,
            item.createdAt,
            item.name,
            item.enabled,
            item.type,
            item.system,
            item.actOnPackageNames.toList(),
            item.taintedOn,
        )
      }
    }
  }
}
