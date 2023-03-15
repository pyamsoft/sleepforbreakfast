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

package com.pyamsoft.sleepforbreakfast.db.transaction

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
internal class TransactionDbImpl
@Inject
internal constructor(
    @DbApi realQueryDao: TransactionQueryDao,
    @DbApi private val realInsertDao: TransactionInsertDao,
    @DbApi private val realDeleteDao: TransactionDeleteDao,
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
        Enforcer.assertOffMainThread()
        return@cachify realQueryDao.query()
      }

  override val deleteDao: TransactionDeleteDao = this

  override val insertDao: TransactionInsertDao = this

  override val queryDao: TransactionQueryDao = this

  override val realtime: TransactionRealtime = this

  override suspend fun invalidate() =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        queryCache.clear()
      }

  override suspend fun listenForChanges(onChange: (event: TransactionChangeEvent) -> Unit) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        onEvent(onChange)
      }

  override suspend fun query(): List<DbTransaction> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext queryCache.call()
      }

  override suspend fun insert(o: DbTransaction): DbInsert.InsertResult<DbTransaction> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext realInsertDao.insert(o).also { result ->
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
                Timber.e(result.error, "Insert attempt failed: ${result.data}")
          }
        }
      }

  override suspend fun delete(o: DbTransaction, offerUndo: Boolean): Boolean =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()
        return@withContext realDeleteDao.delete(o, offerUndo).also { deleted ->
          if (deleted) {
            invalidate()
            publish(TransactionChangeEvent.Delete(o, offerUndo))
          }
        }
      }
}
