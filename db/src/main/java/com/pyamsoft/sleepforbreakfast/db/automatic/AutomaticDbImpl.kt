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

package com.pyamsoft.sleepforbreakfast.db.automatic

import com.pyamsoft.cachify.cachify
import com.pyamsoft.cachify.multiCachify
import com.pyamsoft.pydroid.core.Enforcer
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
internal class AutomaticDbImpl
@Inject
internal constructor(
    @DbApi realQueryDao: AutomaticQueryDao,
    @DbApi private val realInsertDao: AutomaticInsertDao,
    @DbApi private val realDeleteDao: AutomaticDeleteDao,
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

  private val queryCache =
      cachify<List<DbAutomatic>> {
        Enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  private val queryUnusedCache =
      cachify<List<DbAutomatic>> {
        Enforcer.assertOffMainThread()
        return@cachify realQueryDao.queryUnused()
      }

  private val queryByNotificationCache =
      multiCachify<
          QueryByNotificationKey, Maybe<out DbAutomatic>, Int, String, String, String, Long> {
          id,
          key,
          group,
          packageName,
          time ->
        Enforcer.assertOffMainThread()
        return@multiCachify realQueryDao.queryByNotification(
            notificationId = id,
            notificationGroup = group,
            notificationKey = key,
            notificationPostTime = time,
            notificationPackageName = packageName,
        )
      }

  override val deleteDao: AutomaticDeleteDao = this

  override val insertDao: AutomaticInsertDao = this

  override val queryDao: AutomaticQueryDao = this

  override val realtime: AutomaticRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        queryCache.clear()
        queryByNotificationCache.clear()
        queryUnusedCache.clear()
      }

  override suspend fun invalidateByNotification(
      notificationId: Int,
      notificationKey: String,
      notificationGroup: String,
      notificationPackageName: String,
      notificationPostTime: Long
  ) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val key =
            QueryByNotificationKey(
                notificationId = notificationId,
                notificationKey = notificationKey,
                notificationGroup = notificationGroup,
                notificationPostTime = notificationPostTime,
                notificationPackageName = notificationPackageName,
            )

        queryByNotificationCache.key(key).clear()
      }

  override suspend fun listenForChanges(onChange: (event: AutomaticChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        onEvent(onChange)
      }

  override suspend fun query(): List<DbAutomatic> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext queryCache.call()
      }

  override suspend fun queryUnused(): List<DbAutomatic> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext queryUnusedCache.call()
      }

  override suspend fun queryByNotification(
      notificationId: Int,
      notificationKey: String,
      notificationGroup: String,
      notificationPackageName: String,
      notificationPostTime: Long
  ): Maybe<out DbAutomatic> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        val key =
            QueryByNotificationKey(
                notificationId = notificationId,
                notificationKey = notificationKey,
                notificationGroup = notificationGroup,
                notificationPostTime = notificationPostTime,
                notificationPackageName = notificationPackageName,
            )

        return@withContext queryByNotificationCache
            .key(key)
            .call(
                notificationId,
                notificationKey,
                notificationGroup,
                notificationPackageName,
                notificationPostTime,
            )
      }

  override suspend fun insert(o: DbAutomatic): DbInsert.InsertResult<DbAutomatic> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext realInsertDao.insert(o).also { result ->
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
                Timber.e(result.error, "Insert attempt failed: ${result.data}")
          }
        }
      }

  override suspend fun delete(o: DbAutomatic): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext realDeleteDao.delete(o).also { deleted ->
          if (deleted) {
            invalidate()
            publish(AutomaticChangeEvent.Delete(o))
          }
        }
      }

  private data class QueryByNotificationKey(
      val notificationId: Int,
      val notificationKey: String,
      val notificationGroup: String,
      val notificationPackageName: String,
      val notificationPostTime: Long,
  )
}
