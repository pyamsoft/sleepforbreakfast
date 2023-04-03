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

package com.pyamsoft.sleepforbreakfast.transactions.auto

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Stable
data class TransactionAutoParams(
    val autoId: DbAutomatic.Id,
    val autoDate: LocalDate,
) {

  @CheckResult
  fun toJson(): Json {
    return Json(
        autoId = autoId.raw,
        autoDate = DateTimeFormatter.ISO_LOCAL_DATE.format(autoDate),
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json(
      val autoId: String,
      val autoDate: String,
  ) {

    @CheckResult
    fun fromJson(): TransactionAutoParams {
      return TransactionAutoParams(
          autoId = DbAutomatic.Id(autoId),
          autoDate = LocalDate.parse(autoDate, DateTimeFormatter.ISO_LOCAL_DATE),
      )
    }
  }
}