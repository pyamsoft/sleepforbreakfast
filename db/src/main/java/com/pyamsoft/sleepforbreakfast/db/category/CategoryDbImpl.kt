/*
 * Copyright 2021 Peter Kenji Yamanaka
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
import com.pyamsoft.pydroid.core.Enforcer
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
        Enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  override val deleteDao: CategoryDeleteDao = this

  override val insertDao: CategoryInsertDao = this

  override val queryDao: CategoryQueryDao = this

  override val realtime: CategoryRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        queryCache.clear()
      }

  override suspend fun listenForChanges(onChange: (event: CategoryChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        onEvent(onChange)
      }

  override suspend fun query(): List<DbCategory> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext queryCache.call()
      }

  override suspend fun insert(o: DbCategory): DbInsert.InsertResult<DbCategory> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext realInsertDao.insert(o).also { result ->
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
        Enforcer.assertOffMainThread()
        return@withContext realDeleteDao.delete(o).also { deleted ->
          if (deleted) {
            invalidate()
            publish(CategoryChangeEvent.Delete(o))
          }
        }
      }
}
