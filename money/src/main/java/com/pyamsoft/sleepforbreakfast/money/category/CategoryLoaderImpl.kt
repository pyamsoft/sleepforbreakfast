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

package com.pyamsoft.sleepforbreakfast.money.category

import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.category.system.SystemCategories
import com.pyamsoft.sleepforbreakfast.db.category.system.ensure
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class CategoryLoaderImpl
@Inject
constructor(
    private val categoryQueryDao: CategoryQueryDao,
    private val systemCategories: SystemCategories,
) : CategoryLoader {

  override suspend fun queryAllResult(): ResultWrapper<List<DbCategory>> =
      withContext(context = Dispatchers.Default) {
        try {
          ResultWrapper.success(queryAll())
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error getting Categories")
            ResultWrapper.failure(e)
          }
        }
      }

  override suspend fun queryAll(): List<DbCategory> =
      withContext(context = Dispatchers.Default) {
        // This is bad since it constantly queries each time, but its what we've got for now
        systemCategories.ensure()

        return@withContext categoryQueryDao.query()
      }
}
