/*
 * Copyright 2024 pyamsoft
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
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationMatchRegex
import java.time.LocalDateTime

// Kotlin 2.0.20, Room 2.6.1 - Copy annotations currently break Room KSP. Do not use.
@Entity(tableName = RoomDbNotificationMatchRegex.TABLE_NAME)
internal data class RoomDbNotificationMatchRegex
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbNotificationMatchRegex.Id,
    @JvmField @ColumnInfo(name = COLUMN_CREATED_AT) val dbCreatedAt: LocalDateTime,
    @JvmField @ColumnInfo(name = COLUMN_TEXT) val dbText: String,
    @JvmField
    @ColumnInfo(name = COLUMN_NOTIFICATION_ID, index = true)
    val dbNotificationId: DbNotification.Id,
) : DbNotificationMatchRegex {

  @Ignore override val id = dbId

  @Ignore override val createdAt = dbCreatedAt

  @Ignore override val notificationId = dbNotificationId

  @Ignore override val text = dbText

  @Ignore
  override fun text(text: String): DbNotificationMatchRegex {
    return this.copy(dbText = text)
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_notification_match_regex_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_NOTIFICATION_ID = "notification_id"

    @Ignore internal const val COLUMN_CREATED_AT = "created_at"

    @Ignore internal const val COLUMN_TEXT = "text"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbNotificationMatchRegex): RoomDbNotificationMatchRegex {
      return if (item is RoomDbNotificationMatchRegex) item
      else {
        RoomDbNotificationMatchRegex(
            item.id,
            item.createdAt,
            item.text,
            item.notificationId,
        )
      }
    }
  }
}
