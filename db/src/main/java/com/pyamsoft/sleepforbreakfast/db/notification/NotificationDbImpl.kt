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

package com.pyamsoft.sleepforbreakfast.db.notification

import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.ThreadEnforcer
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.BaseDbImpl
import com.pyamsoft.sleepforbreakfast.db.DbApi
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.Maybe
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

@Singleton
internal class NotificationDbImpl
@Inject
internal constructor(
    private val enforcer: ThreadEnforcer,
    @DbApi realQueryDao: NotificationQueryDao,
    @DbApi private val realInsertDao: NotificationInsertDao,
    @DbApi private val realDeleteDao: NotificationDeleteDao,
) :
    NotificationDb,
    NotificationQueryDao.Cache,
    BaseDbImpl<
        NotificationChangeEvent,
        NotificationRealtime,
        NotificationQueryDao,
        NotificationInsertDao,
        NotificationDeleteDao,
    >() {

  private val queryCache = cachify {
    enforcer.assertOffMainThread()
    return@cachify realQueryDao.query()
  }

  private val queryByIdCache =
      multiCachify<QueryByIdKey, Maybe<out DbNotificationWithRegexes>, DbNotification.Id> { id ->
        enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryById(id)
      }

  override val deleteDao: NotificationDeleteDao = this

  override val insertDao: NotificationInsertDao = this

  override val queryDao: NotificationQueryDao = this

  override val realtime: NotificationRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.Default) {
        enforcer.assertOffMainThread()
        queryCache.clear()
        queryByIdCache.clear()
      }

  override suspend fun invalidateById(id: DbNotification.Id) =
      withContext(context = Dispatchers.Default) {
        val key =
            QueryByIdKey(
                id = id,
            )

        queryByIdCache.key(key).clear()
      }

  override fun listenForChanges(): Flow<NotificationChangeEvent> {
    return subscribe()
  }

  override suspend fun query(): List<DbNotificationWithRegexes> =
      withContext(context = Dispatchers.Default) { queryCache.call() }

  override suspend fun queryById(id: DbNotification.Id): Maybe<out DbNotificationWithRegexes> =
      withContext(context = Dispatchers.Default) {
        val key =
            QueryByIdKey(
                id = id,
            )

        return@withContext queryByIdCache.key(key).call(id)
      }

  override suspend fun insert(
      o: DbNotificationWithRegexes
  ): DbInsert.InsertResult<DbNotificationWithRegexes> =
      withContext(context = Dispatchers.Default) {
        realInsertDao.insert(o).also { result ->
          return@also when (result) {
            is DbInsert.InsertResult.Insert -> {
              invalidate()
              publish(NotificationChangeEvent.Insert(result.data))
            }
            is DbInsert.InsertResult.Update -> {
              invalidate()
              publish(NotificationChangeEvent.Update(result.data))
            }
            is DbInsert.InsertResult.Fail ->
                Timber.e(result.error) { "Insert attempt failed: ${result.data}" }
          }
        }
      }

  override suspend fun delete(o: DbNotificationWithRegexes): Boolean =
      withContext(context = Dispatchers.Default) {
        realDeleteDao.delete(o).also { deleted ->
          if (deleted) {
            invalidate()
            publish(NotificationChangeEvent.Delete(o))
          }
        }
      }

  private data class QueryByIdKey(
      val id: DbNotification.Id,
  )
}
