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

package com.pyamsoft.sleepforbreakfast.db.notification

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable

@Stable
interface DbNotificationWithRegexes {

  @get:CheckResult val notification: DbNotification

  @get:CheckResult val matchRegexes: Collection<DbNotificationMatchRegex>

  @CheckResult fun addMatch(match: DbNotificationMatchRegex): DbNotificationWithRegexes

  @CheckResult fun removeMatch(id: DbNotificationMatchRegex.Id): DbNotificationWithRegexes

  private data class Impl(
      override val notification: DbNotification,
      override val matchRegexes: Collection<DbNotificationMatchRegex>,
  ) : DbNotificationWithRegexes {

    override fun addMatch(match: DbNotificationMatchRegex): DbNotificationWithRegexes {
      return this.copy(
          matchRegexes = this.matchRegexes + match,
      )
    }

    override fun removeMatch(id: DbNotificationMatchRegex.Id): DbNotificationWithRegexes {
      return this.copy(
          matchRegexes = this.matchRegexes.filterNot { it.id.raw == id.raw },
      )
    }
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(
        notification: DbNotification,
        regexes: Collection<DbNotificationMatchRegex>,
    ): DbNotificationWithRegexes {
      return Impl(
          notification = notification,
          matchRegexes = regexes,
      )
    }
  }
}

@CheckResult
fun DbNotificationWithRegexes.removeMatch(match: DbNotificationMatchRegex) =
    this.removeMatch(id = match.id)
