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
package com.pyamsoft.sleepforbreakfast.db.room

import com.pyamsoft.sleepforbreakfast.db.SleepDb
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticDb
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDb
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDb
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import javax.inject.Inject

internal class SleepDbImpl
@Inject
internal constructor(
    // DB
    override val transactions: TransactionDb,
    override val categories: CategoryDb,
    override val automatics: AutomaticDb,

    // Caches
    private val transactionCache: TransactionQueryDao.Cache,
    private val categoryCache: CategoryQueryDao.Cache,
    private val automaticCache: AutomaticQueryDao.Cache
) : SleepDb {

  override suspend fun invalidate() {
    transactionCache.invalidate()
    categoryCache.invalidate()
    automaticCache.invalidate()
  }
}
