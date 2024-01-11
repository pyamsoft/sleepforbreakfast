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

package com.pyamsoft.sleepforbreakfast.category

import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.category.CategoryChangeEvent
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDeleteDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryRealtime
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractorImpl
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

internal class CategoryInteractorImpl
@Inject
constructor(
    private val categoryInsertDao: CategoryInsertDao,
    private val categoryDeleteDao: CategoryDeleteDao,
    private val categoryRealtime: CategoryRealtime,
    private val categoryQueryDao: CategoryQueryDao,
    private val categoryQueryCache: CategoryQueryDao.Cache,
    private val categoryLoader: CategoryLoader,
) : CategoryInteractor, ListInteractorImpl<DbCategory.Id, DbCategory, CategoryChangeEvent>() {

  override suspend fun performQueryAll(): List<DbCategory> {
    return categoryLoader.query()
  }

  override suspend fun performQueryOne(id: DbCategory.Id): Maybe<out DbCategory> {
    return categoryQueryDao.queryById(id)
  }

  override suspend fun performClearCache() {
    categoryQueryCache.invalidate()
  }

  override suspend fun performClearCache(id: DbCategory.Id) {
    categoryQueryCache.invalidateById(id)
  }

  override fun listenForItemChanges(): Flow<CategoryChangeEvent> {
    return categoryRealtime.listenForChanges()
  }

  override suspend fun performInsert(item: DbCategory): DbInsert.InsertResult<DbCategory> {
    return categoryInsertDao.insert(item)
  }

  override suspend fun performDelete(item: DbCategory): Boolean {
    return categoryDeleteDao.delete(item)
  }
}
