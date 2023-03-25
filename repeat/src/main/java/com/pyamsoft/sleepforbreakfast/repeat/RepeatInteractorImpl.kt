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

package com.pyamsoft.sleepforbreakfast.repeat

import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatChangeEvent
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDeleteDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatRealtime
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractorImpl
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class RepeatInteractorImpl
@Inject
constructor(
    private val repeatQueryDao: RepeatQueryDao,
    private val repeatQueryCache: RepeatQueryDao.Cache,
    private val repeatInsertDao: RepeatInsertDao,
    private val repeatDeleteDao: RepeatDeleteDao,
    private val repeatRealtime: RepeatRealtime,
) : RepeatInteractor, ListInteractorImpl<DbRepeat.Id, DbRepeat, RepeatChangeEvent>() {

  override suspend fun performQueryAll(): List<DbRepeat> {
    return repeatQueryDao.query()
  }

  override suspend fun performQueryOne(id: DbRepeat.Id): Maybe<out DbRepeat> {
    return repeatQueryDao.queryById(id)
  }

  override suspend fun performClearCache() {
    repeatQueryCache.invalidate()
  }

  override suspend fun performClearCache(id: DbRepeat.Id) {
    repeatQueryCache.invalidateById(id)
  }

  override suspend fun performListenRealtime(onEvent: (RepeatChangeEvent) -> Unit) {
    return repeatRealtime.listenForChanges(onEvent)
  }

  override suspend fun performInsert(item: DbRepeat): DbInsert.InsertResult<DbRepeat> {
    return repeatInsertDao.insert(item)
  }

  override suspend fun performDelete(item: DbRepeat): Boolean {
    return repeatDeleteDao.delete(item)
  }
}
