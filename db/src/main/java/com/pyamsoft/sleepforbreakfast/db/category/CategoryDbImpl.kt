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

package com.pyamsoft.sleepforbreakfast.db.category

import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.sleepforbreakfast.db.BaseDbImpl
import com.pyamsoft.sleepforbreakfast.db.DbApi
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.category.system.RequiredCategories
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class CategoryDbImpl
@Inject
internal constructor(
    private val enforcer: ThreadEnforcer,
    @DbApi realQueryDao: CategoryQueryDao,
    @DbApi private val realInsertDao: CategoryInsertDao,
    @DbApi private val realDeleteDao: CategoryDeleteDao,
) :
    CategoryDb,
    CategoryQueryDao.Cache,
    BaseDbImpl<
        CategoryChangeEvent,
        CategoryRealtime,
        CategoryQueryDao,
        CategoryInsertDao,
        CategoryDeleteDao,
    >() {

  private val queryCache =
      cachify {
        enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  private val queryByIdCache =
      multiCachify<QueryByIdKey, Maybe<out DbCategory>, DbCategory.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryById(id)
      }

  private val queryBySystemCategoryCache =
      multiCachify<QueryBySystemCategoryKey, Maybe<out DbCategory>, RequiredCategories> { category
        ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryBySystemCategory(category)
      }

  override val deleteDao: CategoryDeleteDao = this

  override val insertDao: CategoryInsertDao = this

  override val queryDao: CategoryQueryDao = this

  override val realtime: CategoryRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        queryCache.clear()
        queryByIdCache.clear()
      }

  override suspend fun invalidateById(id: DbCategory.Id) =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryByIdKey(
                categoryId = id,
            )

        queryByIdCache.key(key).clear()
      }

  override suspend fun invalidateBySystemCategory(category: RequiredCategories) =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryBySystemCategoryKey(
                category = category,
            )

        queryBySystemCategoryCache.key(key).clear()
      }

  override suspend fun listenForChanges(onChange: (event: CategoryChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) { onEvent(onChange) }

  override suspend fun query(): List<DbCategory> =
      withContext(context = Dispatchers.IO) { queryCache.call() }

  override suspend fun queryById(id: DbCategory.Id): Maybe<out DbCategory> =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryByIdKey(
                categoryId = id,
            )

        return@withContext queryByIdCache.key(key).call(id)
      }

  override suspend fun queryBySystemCategory(category: RequiredCategories): Maybe<out DbCategory> =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryBySystemCategoryKey(
                category = category,
            )

        return@withContext queryBySystemCategoryCache.key(key).call(category)
      }

  override suspend fun insert(o: DbCategory): DbInsert.InsertResult<DbCategory> =
      withContext(context = Dispatchers.IO) {
        realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> {
              invalidate()
              publish(CategoryChangeEvent.Insert(result.data))
            }
            is DbInsert.InsertResult.Update -> {
              invalidate()
              publish(CategoryChangeEvent.Update(result.data))
            }
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error, "Insert attempt failed: ${result.data}")
          }
        }
      }

  override suspend fun delete(o: DbCategory): Boolean =
      withContext(context = Dispatchers.IO) {
        realDeleteDao.delete(o).also { deleted ->
          if (deleted) {
            invalidate()
            publish(CategoryChangeEvent.Delete(o))
          }
        }
      }

  private data class QueryByIdKey(
      val categoryId: DbCategory.Id,
  )

  private data class QueryBySystemCategoryKey(val category: RequiredCategories)
}
