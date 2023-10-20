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

package com.pyamsoft.sleepforbreakfast.main

import androidx.annotation.CheckResult
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.ui.model.TransactionDateRange

@Stable
sealed interface MainPage {

  @Stable
  @Immutable
  data class Transactions(
      val categoryId: DbCategory.Id,
      val showAllTransactions: Boolean,
      val range: TransactionDateRange?,
  ) : MainPage {

    @CheckResult
    fun toBundleable(): String {
      val s = "$categoryId|$showAllTransactions"
      val rangeData = range?.toBundleable()
      return if (rangeData == null) s
      else {
        "$s|${rangeData.first}|${rangeData.second}"
      }
    }

    companion object {

      @JvmStatic
      @CheckResult
      fun fromBundleable(s: String): MainPage.Transactions {
        val split = s.split("|")
        val rangeDataFrom = split.getOrNull(2)
        val rangeDataTo = split.getOrNull(3)
        return Transactions(
            categoryId = DbCategory.Id(split[0]),
            showAllTransactions = split[1].toBooleanStrict(),
            range =
                if (rangeDataFrom == null || rangeDataTo == null) null
                else {
                  try {
                    TransactionDateRange.fromBundleable(
                        from = rangeDataFrom.toLong(),
                        to = rangeDataTo.toLong(),
                    )
                  } catch (e: Throwable) {
                    Timber.e(e) { "Failed to restore range data from bundle" }
                    null
                  }
                },
        )
      }
    }
  }

  @Stable @Immutable data object Repeat : MainPage

  @Stable @Immutable data object Category : MainPage
}
