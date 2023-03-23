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

package com.pyamsoft.sleepforbreakfast.repeat.delete

import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.money.delete.DeleteViewModeler
import com.pyamsoft.sleepforbreakfast.repeat.RepeatInteractor
import javax.inject.Inject

class RepeatDeleteViewModeler
@Inject
internal constructor(
    state: MutableRepeatDeleteViewState,
    params: RepeatDeleteParams,
    interactor: RepeatInteractor,
) :
    DeleteViewModeler<DbRepeat.Id, DbRepeat, MutableRepeatDeleteViewState>(
        state = state,
        initialId = params.repeatId,
        interactor = interactor,
    ) {

  override fun isIdEmpty(id: DbRepeat.Id): Boolean {
    return id.isEmpty
  }
}
