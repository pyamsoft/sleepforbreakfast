/*
 * Copyright 2025 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.db.transaction

import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.BaseDbImpl
import com.pyamsoft.sleepforbreakfast.db.DbApi
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Singleton
internal class TransactionDbImpl
@Inject
internal constructor(
    private val enforcer: ThreadEnforcer,
    @DbApi realQueryDao: TransactionQueryDao,
    @param:DbApi private val realInsertDao: TransactionInsertDao,
    @param:DbApi private val realDeleteDao: TransactionDeleteDao,
) :
    TransactionDb,
    TransactionQueryDao.Cache,
    BaseDbImpl<
        TransactionChangeEvent,
        TransactionRealtime,
        TransactionQueryDao,
        TransactionInsertDao,
        TransactionDeleteDao,
    >() {

  private val queryCache =
      cachify<List<DbTransaction>> {
        enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  private val queryByIdCache =
      multiCachify<QueryByIdKey, Maybe<out DbTransaction>, DbTransaction.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryById(id)
      }

  private val queryByCategory =
      multiCachify<QueryByCategory, List<DbTransaction>, DbCategory.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryByCategory(id)
      }

  override val deleteDao: TransactionDeleteDao = this

  override val insertDao: TransactionInsertDao = this

  override val queryDao: TransactionQueryDao = this

  override val realtime: TransactionRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.Default) {
        queryCache.clear()
        queryByIdCache.clear()
        queryByCategory.clear()
      }

  override suspend fun invalidateById(id: DbTransaction.Id) =
      withContext(context = Dispatchers.Default) {
        val key = QueryByIdKey(transactionId = id)
        queryByIdCache.key(key).clear()
      }

  override suspend fun invalidateByCategory(id: DbCategory.Id) =
      withContext(context = Dispatchers.Default) {
        val key = QueryByCategory(categoryId = id)
        queryByCategory.key(key).clear()
      }

  override fun listenForChanges(): Flow<TransactionChangeEvent> {
    return subscribe()
  }

  override suspend fun query(): List<DbTransaction> =
      withContext(context = Dispatchers.Default) { queryCache.call() }

  override suspend fun queryById(id: DbTransaction.Id): Maybe<out DbTransaction> =
      withContext(context = Dispatchers.Default) {
        val key = QueryByIdKey(transactionId = id)
        return@withContext queryByIdCache.key(key).call(id)
      }

  override suspend fun queryByCategory(id: DbCategory.Id): List<DbTransaction> =
      withContext(context = Dispatchers.Default) {
        val key = QueryByCategory(categoryId = id)
        return@withContext queryByCategory.key(key).call(id)
      }

  override suspend fun insert(o: DbTransaction): DbInsert.InsertResult<DbTransaction> =
      withContext(context = Dispatchers.Default) {
        realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> {
              invalidate()
              publish(TransactionChangeEvent.Insert(result.data))
            }
            is DbInsert.InsertResult.Update -> {
              invalidate()
              publish(TransactionChangeEvent.Update(result.data))
            }
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error) { "Insert attempt failed: ${result.data}" }
          }
        }
      }

  override suspend fun delete(o: DbTransaction): Boolean =
      withContext(context = Dispatchers.Default) {
        realDeleteDao.delete(o).also { deleted ->
          if (deleted) {
            invalidate()
            publish(TransactionChangeEvent.Delete(o))
          }
        }
      }

  private data class QueryByIdKey(
      val transactionId: DbTransaction.Id,
  )

  private data class QueryByCategory(
      val categoryId: DbCategory.Id,
  )
}
