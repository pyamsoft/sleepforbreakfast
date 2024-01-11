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

package com.pyamsoft.sleepforbreakfast.db.automatic

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.DbQuery
import com.pyamsoft.sleepforbreakfast.db.Maybe

interface AutomaticQueryDao : DbQuery<DbAutomatic> {

  @CheckResult suspend fun queryById(id: DbAutomatic.Id): Maybe<out DbAutomatic>

  @CheckResult suspend fun queryUnused(): List<DbAutomatic>

  @CheckResult
  suspend fun queryByNotification(
      notificationId: Int,
      notificationKey: String,
      notificationGroup: String,
      notificationPackageName: String,
      notificationMatchText: String,
  ): Maybe<out DbAutomatic>

  interface Cache : DbQuery.Cache {

    suspend fun invalidateById(id: DbAutomatic.Id)

    suspend fun invalidateUnused()

    suspend fun invalidateByNotification(
        notificationId: Int,
        notificationKey: String,
        notificationGroup: String,
        notificationPackageName: String,
        notificationMatchText: String,
    )
  }
}

@CheckResult
suspend fun AutomaticQueryDao.queryByAutomaticNotification(
    automatic: DbAutomatic
): Maybe<out DbAutomatic> {
  return this.queryByNotification(
      notificationMatchText = automatic.notificationMatchText,
      notificationKey = automatic.notificationKey,
      notificationGroup = automatic.notificationGroup,
      notificationId = automatic.notificationId,
      notificationPackageName = automatic.notificationPackageName,
  )
}
