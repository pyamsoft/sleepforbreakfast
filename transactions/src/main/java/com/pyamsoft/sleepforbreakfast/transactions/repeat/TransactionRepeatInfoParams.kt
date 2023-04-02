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

package com.pyamsoft.sleepforbreakfast.transactions.repeat

import androidx.annotation.CheckResult
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.squareup.moshi.JsonClass
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Stable
data class TransactionRepeatInfoParams(
    val repeatId: DbRepeat.Id,
    val transactionRepeatDate: LocalDate,
) {

  @CheckResult
  fun toJson(): Json {
    return Json(
        repeatId = repeatId.raw,
        transactionRepeatDate = DateTimeFormatter.ISO_LOCAL_DATE.format(transactionRepeatDate),
    )
  }

  @Stable
  @JsonClass(generateAdapter = true)
  data class Json(
      val repeatId: String,
      val transactionRepeatDate: String,
  ) {

    @CheckResult
    fun fromJson(): TransactionRepeatInfoParams {
      return TransactionRepeatInfoParams(
          repeatId = DbRepeat.Id(repeatId),
          transactionRepeatDate =
              LocalDate.parse(
                  transactionRepeatDate,
                  DateTimeFormatter.ISO_LOCAL_DATE,
              ),
      )
    }
  }
}
