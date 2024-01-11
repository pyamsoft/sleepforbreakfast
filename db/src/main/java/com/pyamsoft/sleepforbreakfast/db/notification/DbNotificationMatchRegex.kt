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

package com.pyamsoft.sleepforbreakfast.db.notification

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import java.time.Clock
import java.time.LocalDateTime

@Stable
interface DbNotificationMatchRegex {

  @get:CheckResult val id: Id

  @get:CheckResult val notificationId: DbNotification.Id

  @get:CheckResult val createdAt: LocalDateTime

  @get:CheckResult val text: String

  @CheckResult fun text(text: String): DbNotificationMatchRegex

  data class Id(
      @get:CheckResult val raw: String,
  ) {

    @get:CheckResult val isEmpty: Boolean = raw.isBlank()

    companion object {

      @JvmField val EMPTY = Id("")
    }
  }

  private data class Impl(
      override val id: Id,
      override val notificationId: DbNotification.Id,
      override val createdAt: LocalDateTime,
      override val text: String,
  ) : DbNotificationMatchRegex {

    override fun text(text: String): DbNotificationMatchRegex {
      return this.copy(text = text)
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        notificationId: DbNotification.Id,
        clock: Clock,
        text: String,
        id: Id = Id(IdGenerator.generate()),
    ): DbNotificationMatchRegex {
      return Impl(
          id = id,
          createdAt = LocalDateTime.now(clock),
          notificationId = notificationId,
          text = text,
      )
    }
  }
}
