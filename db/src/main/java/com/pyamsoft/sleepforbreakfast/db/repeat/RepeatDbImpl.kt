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

package com.pyamsoft.sleepforbreakfast.db.repeat

import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.BaseDbImpl
import com.pyamsoft.sleepforbreakfast.db.DbApi
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Singleton
internal class RepeatDbImpl
@Inject
internal constructor(
    private val enforcer: ThreadEnforcer,
    @DbApi realQueryDao: RepeatQueryDao,
    @DbApi private val realInsertDao: RepeatInsertDao,
    @DbApi private val realDeleteDao: RepeatDeleteDao,

    // When we delete DbRepeats, invalidate the DbTransaction table so it can re-fetch information
    private val transactionQueryCache: TransactionQueryDao.Cache,
) :
    RepeatDb,
    RepeatQueryDao.Cache,
    BaseDbImpl<
        RepeatChangeEvent,
        RepeatRealtime,
        RepeatQueryDao,
        RepeatInsertDao,
        RepeatDeleteDao,
    >() {

  private val queryCache = cachify {
    enforcer.assertOffMainThread()
    return@cachify realQueryDao.query()
  }

  private val queryActiveCache = cachify {
    enforcer.assertOffMainThread()
    return@cachify realQueryDao.queryActive()
  }

  private val queryByIdCache =
      multiCachify<QueryByIdKey, Maybe<out DbRepeat>, DbRepeat.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryById(id)
      }

  override val deleteDao: RepeatDeleteDao = this

  override val insertDao: RepeatInsertDao = this

  override val queryDao: RepeatQueryDao = this

  override val realtime: RepeatRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.Default) {
        queryCache.clear()
        queryByIdCache.clear()
        queryActiveCache.clear()
      }

  override suspend fun invalidateActive() =
      withContext(context = Dispatchers.Default) { queryActiveCache.clear() }

  override suspend fun invalidateById(id: DbRepeat.Id) =
      withContext(context = Dispatchers.Default) {
        val key =
            QueryByIdKey(
                repeatId = id,
            )

        queryByIdCache.key(key).clear()
      }

  override fun listenForChanges(): Flow<RepeatChangeEvent> {
    return subscribe()
  }

  override suspend fun query(): List<DbRepeat> =
      withContext(context = Dispatchers.Default) { queryCache.call() }

  override suspend fun queryActive(): List<DbRepeat> =
      withContext(context = Dispatchers.Default) { queryActiveCache.call() }

  override suspend fun queryById(id: DbRepeat.Id): Maybe<out DbRepeat> =
      withContext(context = Dispatchers.Default) {
        val key =
            QueryByIdKey(
                repeatId = id,
            )

        return@withContext queryByIdCache.key(key).call(id)
      }

  override suspend fun insert(o: DbRepeat): DbInsert.InsertResult<DbRepeat> =
      withContext(context = Dispatchers.Default) {
        realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> {
              invalidate()
              publish(RepeatChangeEvent.Insert(result.data))
            }
            is DbInsert.InsertResult.Update -> {
              invalidate()
              publish(RepeatChangeEvent.Update(result.data))
            }
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error) { "Insert attempt failed: ${result.data}" }
          }
        }
      }

  override suspend fun delete(o: DbRepeat): Boolean =
      withContext(context = Dispatchers.Default) {
        realDeleteDao.delete(o).also { deleted ->
          if (deleted) {
            invalidate()

            // Also clear the TransactionQueryCache (in case a delete cascades)
            transactionQueryCache.invalidate()

            publish(RepeatChangeEvent.Delete(o))
          }
        }
      }

  private data class QueryByIdKey(
      val repeatId: DbRepeat.Id,
  )
}
