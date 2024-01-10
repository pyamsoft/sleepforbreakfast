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

package com.pyamsoft.sleepforbreakfast.spending.automatic

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.spending.AutomaticHandler

internal class DbAutomaticHandler
private constructor(
    private val notification: DbNotificationWithRegexes,
) : BaseAutomaticHandler() {

  private val regexes by lazy {
    notification.matchRegexes.map { n ->
      RegexMatch(
          id = n.id.raw,
          regex = n.text.toRegex(RegexOption.MULTILINE),
      )
    }
  }

  override fun getPossibleRegexes(): Collection<RegexMatch> {
    return regexes
  }

  override fun getType(): DbTransaction.Type {
    return notification.notification.type
  }

  override fun canExtract(packageName: String): Boolean {
    return packageName in notification.notification.actOnPackageNames
  }

  companion object {

    @JvmStatic
    @CheckResult
    fun create(notification: DbNotificationWithRegexes): AutomaticHandler {
      return DbAutomaticHandler(
          notification = notification,
      )
    }
  }
}
