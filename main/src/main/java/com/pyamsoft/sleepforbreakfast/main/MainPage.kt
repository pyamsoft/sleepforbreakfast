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
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory

@Stable
sealed interface MainPage {

  @Stable
  @Immutable
  data class Transactions(
      val categoryId: DbCategory.Id,
      val showAllTransactions: Boolean,
  ) : MainPage {

    @CheckResult
    fun asSaveable(): String {
      return "$categoryId|$showAllTransactions"
    }

    companion object {

      @JvmStatic
      @CheckResult
      fun fromSaveable(s: String): MainPage.Transactions {
        val split = s.split("|")
        return Transactions(
            categoryId = DbCategory.Id(split[0]),
            showAllTransactions = split[1].toBooleanStrict(),
        )
      }
    }
  }

  @Stable @Immutable data object Repeat : MainPage

  @Stable @Immutable data object Category : MainPage
}
