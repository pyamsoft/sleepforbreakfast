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
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.BaseDbImpl
import com.pyamsoft.sleepforbreakfast.db.DbApi
import com.pyamsoft.sleepforbreakfast.db.DbInsert
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
      cachify<List<DbCategory>> {
        enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  private val queryByIdCache =
      multiCachify<QueryByIdKey, Maybe<out DbCategory>, DbCategory.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryById(id)
      }

  private val queryBySystemNameCache =
      multiCachify<QueryBySystemNameKey, Maybe<out DbCategory>, String> { name ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryBySystemName(name)
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

  override suspend fun invalidateBySystemName(name: String) =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryBySystemNameKey(
                name = name,
            )

        queryBySystemNameCache.key(key).clear()
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

  override suspend fun queryBySystemName(name: String): Maybe<out DbCategory> =
      withContext(context = Dispatchers.IO) {
        val key =
            QueryBySystemNameKey(
                name = name,
            )

        return@withContext queryBySystemNameCache.key(key).call(name)
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

  private data class QueryBySystemNameKey(val name: String)
}
