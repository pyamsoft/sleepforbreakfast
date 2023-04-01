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

package com.pyamsoft.sleepforbreakfast.db.category.system

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.core.IdGenerator
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.category.CategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
  private fun noteForName(category: RequiredCategories): String {
    val what =
        when (category) {
          RequiredCategories.VENMO -> "Venmo related transactions"
          RequiredCategories.VENMO_PAY -> "Venmo payments"
          RequiredCategories.VENMO_REQUESTS -> "Venmo requests"
          RequiredCategories.GOOGLE_WALLET -> "Google Wallet spending notifications"
          RequiredCategories.REPEATING -> "Repeating Transactions"
        }

    return "System Category for $what"
  }

  @CheckResult
  private fun createSystemCategory(category: RequiredCategories): DbCategory {
    return DbCategory.create(
            clock,
            id = DbCategory.Id(IdGenerator.generate()),
        )
        .systemLevel()
        .name(category.displayName)
        .note(noteForName(category))
  }

  @CheckResult
  private suspend fun ensure(category: RequiredCategories): DbCategory? =
      GLOBAL_LOCK.withLock {
        when (val existing = categoryQueryDao.queryBySystemCategory(category)) {
          is Maybe.Data -> {
            Timber.d("RequiredCategory already exists: $category")
            return@withLock existing.data
          }
          is Maybe.None -> {
            when (val res = categoryInsertDao.insert(createSystemCategory(category))) {
              is DbInsert.InsertResult.Fail -> {
                Timber.e(res.error, "Failed inserting RequiredCategory: $category")
                return@withLock null
              }
              is DbInsert.InsertResult.Insert -> {
                Timber.d("Inserted new RequiredCategory: $category")
                return@withLock res.data
              }
              is DbInsert.InsertResult.Update -> {
                Timber.d("Updated RequiredCategory, should this happen?: $category")
                return@withLock res.data
              }
            }
          }
        }
      }

  override suspend fun categoryByName(category: RequiredCategories): DbCategory? =
      withContext(context = Dispatchers.IO) { ensure(category) }

  companion object {
    /**
     * the global lock prevents multiple callers from running this handler at the same time as it
     * could cause duplicates in the DB if operations are close enough
     */
    private val GLOBAL_LOCK = Mutex()
  }
}
