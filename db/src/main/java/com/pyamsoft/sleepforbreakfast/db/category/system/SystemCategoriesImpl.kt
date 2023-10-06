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
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.category.CategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

internal class SystemCategoriesImpl
@Inject
internal constructor(
    private val categoryQueryDao: CategoryQueryDao,
    private val categoryInsertDao: CategoryInsertDao,
    private val clock: Clock,
) : SystemCategories {

  @CheckResult
  private fun createSystemCategory(category: RequiredCategories): DbCategory {
    return DbCategory.create(
            clock,
            id = DbCategory.Id(IdGenerator.generate()),
        )
        .name(category.displayName)
        .activate()
        .unarchive()
  }

  override suspend fun create(category: RequiredCategories): DbCategory? =
      withContext(context = Dispatchers.Default) {
        GLOBAL_LOCK.withLock {
          val cats = categoryQueryDao.query()
          Timber.d {
            "RequiredCategory: ALL CATS: ${cats.map { it.name }}  ?? ${category.displayName}"
          }

          when (val res = categoryInsertDao.insert(createSystemCategory(category))) {
            is DbInsert.InsertResult.Fail -> {
              Timber.e(res.error) { "Failed inserting RequiredCategory: $category" }
              return@withLock null
            }
            is DbInsert.InsertResult.Insert -> {
              Timber.d { "Inserted new RequiredCategory: $category" }
              return@withLock res.data
            }
            is DbInsert.InsertResult.Update -> {
              Timber.d { "Updated RequiredCategory, should this happen?: $category" }
              return@withLock res.data
            }
          }
        }
      }

  companion object {
    /**
     * the global lock prevents multiple callers from running this handler at the same time as it
     * could cause duplicates in the DB if operations are close enough
     */
    private val GLOBAL_LOCK = Mutex()
  }
}
