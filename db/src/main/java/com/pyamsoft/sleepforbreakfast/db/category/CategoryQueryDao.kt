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

package com.pyamsoft.sleepforbreakfast.db.category

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.DbQuery
import com.pyamsoft.sleepforbreakfast.db.Maybe
import com.pyamsoft.sleepforbreakfast.db.category.system.RequiredCategories

interface CategoryQueryDao : DbQuery<DbCategory> {

  @CheckResult suspend fun queryById(id: DbCategory.Id): Maybe<out DbCategory>

  @CheckResult
  suspend fun queryBySystemCategory(category: RequiredCategories): Maybe<out DbCategory>

  interface Cache : DbQuery.Cache {

    suspend fun invalidateById(id: DbCategory.Id)

    suspend fun invalidateBySystemCategory(category: RequiredCategories)
  }
}
