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

package com.pyamsoft.sleepforbreakfast.db.transaction

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbQuery
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import java.time.LocalDate

interface TransactionQueryDao : DbQuery<DbTransaction> {

  @CheckResult suspend fun queryById(id: DbTransaction.Id): Maybe<out DbTransaction>

  @CheckResult suspend fun queryByRepeat(id: DbRepeat.Id): List<DbTransaction>

  @CheckResult
  suspend fun queryByRepeatOnDate(
      id: DbRepeat.Id,
      date: LocalDate,
  ): Maybe<out DbTransaction>

  interface Cache : DbQuery.Cache {

    suspend fun invalidateById(id: DbTransaction.Id)

    suspend fun invalidateByRepeat(id: DbRepeat.Id)

    suspend fun invalidateByRepeatOnDate(
        id: DbRepeat.Id,
        date: LocalDate,
    )
  }
}
