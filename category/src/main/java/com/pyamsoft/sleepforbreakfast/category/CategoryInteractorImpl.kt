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

package com.pyamsoft.sleepforbreakfast.category

import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.category.CategoryChangeEvent
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDeleteDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryRealtime
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.category.system.SystemCategories
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
internal class CategoryInteractorImpl
@Inject
constructor(
    private val categoryInsertDao: CategoryInsertDao,
    private val categoryDeleteDao: CategoryDeleteDao,
    private val categoryRealtime: CategoryRealtime,
    private val categoryQueryDao: CategoryQueryDao,
    private val categoryQueryCache: CategoryQueryDao.Cache,
    private val systemCategories: SystemCategories,
) : CategoryInteractor, ListInteractorImpl<DbCategory.Id, DbCategory, CategoryChangeEvent>() {

  private suspend fun ensureSystemCategory(category: SystemCategories.Categories) {
    try {
      if (systemCategories.categoryByName(category) == null) {
        Timber.w("Failed to create system category $category")
      }
    } catch (e: Throwable) {
      Timber.e(e, "Error creating system category $category")
    }
  }

  private suspend fun ensureSystemCategoriesExist() {
    // Venmo
    ensureSystemCategory(SystemCategories.Categories.VENMO)
    ensureSystemCategory(SystemCategories.Categories.VENMO_PAY)
    ensureSystemCategory(SystemCategories.Categories.VENMO_REQUESTS)

    // Google wallet
    ensureSystemCategory(SystemCategories.Categories.GOOGLE_WALLET)
  }

  override suspend fun performQueryAll(): List<DbCategory> {
    // This is bad since it constantly queries each time, but its what we've got for now
    ensureSystemCategoriesExist()

    return categoryQueryDao.query()
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

  override suspend fun performListenRealtime(onEvent: (CategoryChangeEvent) -> Unit) {
    categoryRealtime.listenForChanges(onEvent)
  }

  override suspend fun performInsert(item: DbCategory): DbInsert.InsertResult<DbCategory> {
    return categoryInsertDao.insert(item)
  }

  override suspend fun performDelete(item: DbCategory): Boolean {
    return categoryDeleteDao.delete(item)
  }
}
