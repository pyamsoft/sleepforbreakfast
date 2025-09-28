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
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationMatchRegex
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes

@ConsistentCopyVisibility
internal data class RoomDbNotificationWithRegexes
internal constructor(
    @JvmField @Embedded val dbNotification: RoomDbNotification,
    @JvmField
    @Relation(
        parentColumn = RoomDbNotification.COLUMN_ID,
        entityColumn = RoomDbNotificationMatchRegex.COLUMN_NOTIFICATION_ID,
    )
    val dbMatchRegexes: List<RoomDbNotificationMatchRegex>,
) : DbNotificationWithRegexes {

  @Ignore override val matchRegexes = dbMatchRegexes

  @Ignore override val notification = dbNotification

  @Ignore
  override fun addMatch(match: DbNotificationMatchRegex): DbNotificationWithRegexes {
    return this.copy(
        dbMatchRegexes = this.dbMatchRegexes + RoomDbNotificationMatchRegex.create(match),
    )
  }

  @Ignore
  override fun removeMatch(id: DbNotificationMatchRegex.Id): DbNotificationWithRegexes {
    return this.copy(
        dbMatchRegexes = this.dbMatchRegexes.filterNot { it.id.raw == id.raw },
    )
  }

  companion object {

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbNotificationWithRegexes): RoomDbNotificationWithRegexes {
      return if (item is RoomDbNotificationWithRegexes) item
      else {
        RoomDbNotificationWithRegexes(
            RoomDbNotification.create(item.notification),
            item.matchRegexes.map { RoomDbNotificationMatchRegex.create(it) },
        )
      }
    }
  }
}
