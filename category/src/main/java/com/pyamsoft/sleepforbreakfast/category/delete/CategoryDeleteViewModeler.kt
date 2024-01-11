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

package com.pyamsoft.sleepforbreakfast.category.delete

import com.pyamsoft.sleepforbreakfast.category.CategoryInteractor
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.money.delete.DeleteViewModeler
import javax.inject.Inject

class CategoryDeleteViewModeler
@Inject
internal constructor(
    state: MutableCategoryDeleteViewState,
    params: CategoryDeleteParams,
    interactor: CategoryInteractor,
) :
    CategoryDeleteViewState by state,
    DeleteViewModeler<DbCategory.Id, DbCategory, MutableCategoryDeleteViewState>(
        state = state,
        initialId = params.categoryId,
        interactor = interactor,
    ) {

  override fun isIdEmpty(id: DbCategory.Id): Boolean {
    return id.isEmpty
  }
}
