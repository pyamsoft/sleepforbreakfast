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

package com.pyamsoft.sleepforbreakfast.transactions

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionChangeEvent
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDeleteDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionRealtime
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class TransactionInteractorImpl
@Inject
constructor(
    private val transactionRealtime: TransactionRealtime,
    private val transactionInsertDao: TransactionInsertDao,
    private val transactionDeleteDao: TransactionDeleteDao,
    private val transactionQueryDao: TransactionQueryDao,
    private val transactionQueryCache: TransactionQueryDao.Cache,
    private val categoryQueryDao: CategoryQueryDao,
) :
    TransactionInteractor,
    ListInteractorImpl<DbTransaction.Id, DbTransaction, TransactionChangeEvent>() {

  override suspend fun categories(): ResultWrapper<List<DbCategory>> =
      withContext(context = Dispatchers.IO) {
        try {
          ResultWrapper.success(categoryQueryDao.query())
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting Categories")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun performQueryAll(): List<DbTransaction> {
    return transactionQueryDao.query()
  }

  override suspend fun performQueryOne(id: DbTransaction.Id): Maybe<out DbTransaction> {
    return transactionQueryDao.queryById(id)
  }

  override suspend fun performClearCache() {
    transactionQueryCache.invalidate()
  }

  override suspend fun performClearCache(id: DbTransaction.Id) {
    transactionQueryCache.invalidateById(id)
  }

  override suspend fun performListenRealtime(onEvent: (TransactionChangeEvent) -> Unit) {
    transactionRealtime.listenForChanges(onEvent)
  }

  override suspend fun performInsert(item: DbTransaction): DbInsert.InsertResult<DbTransaction> {
    return transactionInsertDao.insert(item)
  }

  override suspend fun performDelete(item: DbTransaction): Boolean {
    return transactionDeleteDao.delete(item)
  }
}
