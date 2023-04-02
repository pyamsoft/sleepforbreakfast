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

package com.pyamsoft.sleepforbreakfast.transactions.repeat

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
internal class TransactionRepeatInteractorImpl
@Inject
internal constructor(
    private val repeatQueryDao: RepeatQueryDao,
) : TransactionRepeatInteractor {

  override suspend fun getById(id: DbRepeat.Id): ResultWrapper<DbRepeat> =
      withContext(context = Dispatchers.IO) {
        return@withContext try {
          when (val r = repeatQueryDao.queryById(id)) {
            is Maybe.Data -> ResultWrapper.success(r.data)
            is Maybe.None -> ResultWrapper.failure(RuntimeException("Missing repeat with id: $id"))
          }
        } catch (e: Throwable) {
          Timber.e(e, "Error getting repeat from db: $id")
          ResultWrapper.failure(e)
        }
      }
}
