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

package com.pyamsoft.sleepforbreakfast.sources.delete

import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.money.delete.DeleteViewModeler
import com.pyamsoft.sleepforbreakfast.sources.SourcesInteractor
import javax.inject.Inject

class SourcesDeleteViewModeler
@Inject
internal constructor(
    state: MutableSourcesDeleteViewState,
    params: SourcesDeleteParams,
    interactor: SourcesInteractor,
    deleteInteractor: SourcesDeleteInteractor,
) :
    DeleteViewModeler<DbSource.Id, DbSource, MutableSourcesDeleteViewState>(
        state = state,
        initialId = params.sourcesId,
        interactor = interactor,
        deleteInteractor = deleteInteractor,
    ) {

  override fun isIdEmpty(id: DbSource.Id): Boolean {
    return id.isEmpty
  }
}
