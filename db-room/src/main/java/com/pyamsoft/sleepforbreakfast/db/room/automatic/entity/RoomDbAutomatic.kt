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

package com.pyamsoft.sleepforbreakfast.db.room.automatic.entity

import androidx.annotation.CheckResult
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic

@Entity(tableName = RoomDbAutomatic.TABLE_NAME, indices = [])
internal data class RoomDbAutomatic
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbAutomatic.Id,
    @JvmField @ColumnInfo(name = COLUMN_NOTIFICATION_ID) val dbNotificationId: Int,
    @JvmField @ColumnInfo(name = COLUMN_NOTIFICATION_KEY) val dbNotificationKey: String,
    @JvmField @ColumnInfo(name = COLUMN_NOTIFICATION_GROUP) val dbNotificationGroup: String,
    @JvmField @ColumnInfo(name = COLUMN_NOTIFICATION_PACKAGE) val dbNotificationPackage: String,
    @JvmField @ColumnInfo(name = COLUMN_NOTIFICATION_POST_TIME) val dbNotificationPostTime: Long,
    @JvmField @ColumnInfo(name = COLUMN_NOTIFICATION_MATCH_TEXT) val dbNotificationMatches: String,
    @JvmField @ColumnInfo(name = COLUMN_NOTIFICATION_AMOUNT) val dbNotificationAmount: Long,
    @JvmField @ColumnInfo(name = COLUMN_NOTIFICATION_TITLE) val dbNotificationTitle: String,
    @JvmField @ColumnInfo(name = COLUMN_USED) val dbUsed: Boolean,
) : DbAutomatic {

  @Ignore override val id = dbId

  @Ignore override val notificationId = dbNotificationId

  @Ignore override val notificationKey = dbNotificationKey

  @Ignore override val notificationGroup = dbNotificationGroup

  @Ignore override val notificationPackageName = dbNotificationPackage

  @Ignore override val notificationPostTime = dbNotificationPostTime

  @Ignore override val notificationMatchText = dbNotificationMatches

  @Ignore override val notificationAmountInCents = dbNotificationAmount

  @Ignore override val notificationTitle = dbNotificationTitle

  @Ignore override val used = dbUsed

  @Ignore
  override fun notificationId(id: Int): DbAutomatic {
    return this.copy(dbNotificationId = id)
  }

  @Ignore
  override fun notificationKey(key: String): DbAutomatic {
    return this.copy(dbNotificationKey = key)
  }

  @Ignore
  override fun notificationGroup(group: String): DbAutomatic {
    return this.copy(dbNotificationGroup = group)
  }

  @Ignore
  override fun notificationPackageName(packageName: String): DbAutomatic {
    return this.copy(dbNotificationPackage = packageName)
  }

  @Ignore
  override fun notificationPostTime(time: Long): DbAutomatic {
    return this.copy(dbNotificationPostTime = time)
  }

  @Ignore
  override fun notificationMatchText(text: String): DbAutomatic {
    return this.copy(dbNotificationMatches = text)
  }

  @Ignore
  override fun notificationAmountInCents(amount: Long): DbAutomatic {
    return this.copy(dbNotificationAmount = amount)
  }

  @Ignore
  override fun notificationTitle(title: String): DbAutomatic {
    return this.copy(dbNotificationTitle = title)
  }

  @Ignore
  override fun consume(): DbAutomatic {
    return this.copy(dbUsed = true)
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_automatics_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_NOTIFICATION_ID = "notification_id"

    @Ignore internal const val COLUMN_NOTIFICATION_KEY = "notification_key"

    @Ignore internal const val COLUMN_NOTIFICATION_GROUP = "notification_group"

    @Ignore internal const val COLUMN_NOTIFICATION_PACKAGE = "notification_package_name"

    @Ignore internal const val COLUMN_NOTIFICATION_POST_TIME = "notification_post_time"

    @Ignore internal const val COLUMN_NOTIFICATION_MATCH_TEXT = "notification_match_text"

    @Ignore internal const val COLUMN_NOTIFICATION_AMOUNT = "notification_amount_in_cents"

    @Ignore internal const val COLUMN_NOTIFICATION_TITLE = "notification_title"

    @Ignore internal const val COLUMN_USED = "used"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbAutomatic): RoomDbAutomatic {
      return if (item is RoomDbAutomatic) item
      else {
        RoomDbAutomatic(
            item.id,
            item.notificationId,
            item.notificationKey,
            item.notificationGroup,
            item.notificationPackageName,
            item.notificationPostTime,
            item.notificationMatchText,
            item.notificationAmountInCents,
            item.notificationTitle,
            item.used,
        )
      }
    }
  }
}
