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

package com.pyamsoft.sleepforbreakfast.spending.db

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.category.CategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class SystemCategoriesImpl
@Inject
internal constructor(
    private val categoryQueryDao: CategoryQueryDao,
    private val categoryInsertDao: CategoryInsertDao,
    private val clock: Clock,
) : SystemCategories {

  @CheckResult
  private fun noteForName(category: SystemCategories.Categories): String {
    val what =
        when (category) {
          SystemCategories.Categories.VENMO -> "Venmo related transactions"
          SystemCategories.Categories.VENMO_PAY -> "Venmo payments"
          SystemCategories.Categories.VENMO_REQUESTS -> "Venmo requests"
          SystemCategories.Categories.GOOGLE_WALLET -> "Google Wallet spending notifications"
        }

    return "System Category for $what"
  }

  @CheckResult
  private fun createSystemCategory(category: SystemCategories.Categories): DbCategory {
    return DbCategory.create(clock, id = DbCategory.Id(IdGenerator.generate()))
        .systemLevel()
        .name(category.displayName)
        .note(noteForName(category))
  }

  override suspend fun categoryByName(category: SystemCategories.Categories): DbCategory? =
      withContext(context = Dispatchers.IO) {
        when (val existing = categoryQueryDao.queryBySystemName(category.displayName)) {
          is Maybe.Data -> existing.data
          is Maybe.None -> {
            val db = createSystemCategory(category)
            when (val result = categoryInsertDao.insert(db)) {
              is DbInsert.InsertResult.Fail -> {
                Timber.e(result.error, "Failed to insert system category: $db")
                return@withContext null
              }
              is DbInsert.InsertResult.Insert -> {
                Timber.d("Inserted new system category: $db")
                return@withContext result.data
              }
              is DbInsert.InsertResult.Update -> {
                // Should this happen
                Timber.d("Updated existing system category: $db")
                return@withContext result.data
              }
            }
          }
        }
      }
}
