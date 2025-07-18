/*
 * Copyright 2025 pyamsoft
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

import com.pyamsoft.sleepforbreakfast.db.category.CategoryChangeEvent
import com.pyamsoft.sleepforbreakfast.db.category.CategoryRealtime
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DefaultCategoryObserver
@Inject
internal constructor(
    loader: CategoryLoader,
    categoryRealtime: CategoryRealtime,
) :
    CategoryObserver,
    AbstractDbObserver<DbCategory, CategoryChangeEvent, DbCategory.Id>(
        query = loader,
        realtime = categoryRealtime,
    ) {

  override val emptyInstance = DbCategory.NONE

  override fun dataToId(data: DbCategory): DbCategory.Id {
    return data.id
  }

  override fun isEmpty(data: DbCategory): Boolean {
    return dataToId(data).isEmpty
  }

  override fun sortData(data: DbCategory): String {
    return data.name.lowercase()
  }

  override suspend fun onRealtimeEvent(event: CategoryChangeEvent) =
      when (event) {
        is CategoryChangeEvent.Delete -> handleRealtimeDelete(event.category)
        is CategoryChangeEvent.Insert -> handleRealtimeInsert(event.category)
        is CategoryChangeEvent.Update -> handleRealtimeUpdate(event.category)
      }
}
