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

package com.pyamsoft.sleepforbreakfast.db.automatic

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
internal class AutomaticDbImpl
@Inject
internal constructor(
    private val enforcer: ThreadEnforcer,
    @DbApi realQueryDao: AutomaticQueryDao,
    @DbApi private val realInsertDao: AutomaticInsertDao,
    @DbApi private val realDeleteDao: AutomaticDeleteDao,

    // When we delete DbAutomatics, invalidate the DbTransaction table so it can re-fetch
    // information
    private val transactionQueryCache: TransactionQueryDao.Cache,
) :
    AutomaticDb,
    AutomaticQueryDao.Cache,
    BaseDbImpl<
        AutomaticChangeEvent,
        AutomaticRealtime,
        AutomaticQueryDao,
        AutomaticInsertDao,
        AutomaticDeleteDao,
    >() {

  private val queryCache = cachify {
    enforcer.assertOffMainThread()
    return@cachify realQueryDao.query()
  }

  private val queryByIdCache =
      multiCachify<QueryByIdKey, Maybe<out DbAutomatic>, DbAutomatic.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryById(id)
      }

  private val queryUnusedCache = cachify {
    enforcer.assertOffMainThread()
    return@cachify realQueryDao.queryUnused()
  }

  private val queryByNotificationCache =
      multiCachify<
          QueryByNotificationKey, Maybe<out DbAutomatic>, Int, String, String, String, String> {
          id,
          key,
          group,
          packageName,
          matchText ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryByNotification(
            notificationId = id,
            notificationGroup = group,
            notificationKey = key,
            notificationMatchText = matchText,
            notificationPackageName = packageName,
        )
      }

  override val deleteDao: AutomaticDeleteDao = this

  override val insertDao: AutomaticInsertDao = this

  override val queryDao: AutomaticQueryDao = this

  override val realtime: AutomaticRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.Default) {
        enforcer.assertOffMainThread()
        queryCache.clear()
        queryByNotificationCache.clear()
        queryUnusedCache.clear()
        queryByIdCache.clear()
      }

  override suspend fun invalidateUnused() =
      withContext(context = Dispatchers.Default) { queryUnusedCache.clear() }

  override suspend fun invalidateByNotification(
      notificationId: Int,
      notificationKey: String,
      notificationGroup: String,
      notificationPackageName: String,
      notificationMatchText: String
  ) =
      withContext(context = Dispatchers.Default) {
        val key =
            QueryByNotificationKey(
                notificationId = notificationId,
                notificationKey = notificationKey,
                notificationGroup = notificationGroup,
                notificationMatchText = notificationMatchText,
                notificationPackageName = notificationPackageName,
            )

        queryByNotificationCache.key(key).clear()
      }

  override suspend fun invalidateById(id: DbAutomatic.Id) =
      withContext(context = Dispatchers.Default) {
        val key =
            QueryByIdKey(
                id = id,
            )

        queryByIdCache.key(key).clear()
      }

  override fun listenForChanges(): Flow<AutomaticChangeEvent> {
    return subscribe()
  }

  override suspend fun query(): List<DbAutomatic> =
      withContext(context = Dispatchers.Default) { queryCache.call() }

  override suspend fun queryUnused(): List<DbAutomatic> =
      withContext(context = Dispatchers.Default) { queryUnusedCache.call() }

  override suspend fun queryByNotification(
      notificationId: Int,
      notificationKey: String,
      notificationGroup: String,
      notificationPackageName: String,
      notificationMatchText: String
  ): Maybe<out DbAutomatic> =
      withContext(context = Dispatchers.Default) {
        val key =
            QueryByNotificationKey(
                notificationId = notificationId,
                notificationKey = notificationKey,
                notificationGroup = notificationGroup,
                notificationMatchText = notificationMatchText,
                notificationPackageName = notificationPackageName,
            )

        return@withContext queryByNotificationCache
            .key(key)
            .call(
                notificationId,
                notificationKey,
                notificationGroup,
                notificationPackageName,
                notificationMatchText,
            )
      }

  override suspend fun queryById(id: DbAutomatic.Id): Maybe<out DbAutomatic> =
      withContext(context = Dispatchers.Default) {
        val key =
            QueryByIdKey(
                id = id,
            )

        return@withContext queryByIdCache.key(key).call(id)
      }

  override suspend fun insert(o: DbAutomatic): DbInsert.InsertResult<DbAutomatic> =
      withContext(context = Dispatchers.Default) {
        realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> {
              invalidate()
              publish(AutomaticChangeEvent.Insert(result.data))
            }
            is DbInsert.InsertResult.Update -> {
              invalidate()
              publish(AutomaticChangeEvent.Update(result.data))
            }
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error) { "Insert attempt failed: ${result.data}" }
          }
        }
      }

  override suspend fun delete(o: DbAutomatic): Boolean =
      withContext(context = Dispatchers.Default) {
        realDeleteDao.delete(o).also { deleted ->
          if (deleted) {
            invalidate()

            // Also clear the TransactionQueryCache (in case a delete cascades)
            transactionQueryCache.invalidate()

            publish(AutomaticChangeEvent.Delete(o))
          }
        }
      }

  private data class QueryByIdKey(
      val id: DbAutomatic.Id,
  )

  private data class QueryByNotificationKey(
      val notificationId: Int,
      val notificationKey: String,
      val notificationGroup: String,
      val notificationPackageName: String,
      val notificationMatchText: String,
  )
}
