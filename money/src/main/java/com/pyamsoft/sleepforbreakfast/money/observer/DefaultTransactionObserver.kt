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

package com.pyamsoft.sleepforbreakfast.money.observer

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionChangeEvent
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionRealtime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultTransactionObserver
@Inject
internal constructor(
    query: TransactionQueryDao,
    categoryRealtime: TransactionRealtime,
) :
    TransactionObserver,
    AbstractDbObserver<DbTransaction, TransactionChangeEvent, DbTransaction.Id>(
        query = query,
        realtime = categoryRealtime,
    ) {

  override val emptyInstance = DbTransaction.NONE

  override fun dataToId(data: DbTransaction): DbTransaction.Id {
    return data.id
  }

  override fun isEmpty(data: DbTransaction): Boolean {
    return dataToId(data).isEmpty
  }

  override fun sortData(data: DbTransaction): String {
    return data.name.lowercase()
  }

  override suspend fun onRealtimeEvent(event: TransactionChangeEvent) =
      when (event) {
        is TransactionChangeEvent.Delete -> handleRealtimeDelete(event.transaction)
        is TransactionChangeEvent.Insert -> handleRealtimeInsert(event.transaction)
        is TransactionChangeEvent.Update -> handleRealtimeUpdate(event.transaction)
      }
}
