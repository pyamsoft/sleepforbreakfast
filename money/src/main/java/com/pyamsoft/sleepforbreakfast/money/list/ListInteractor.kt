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

package com.pyamsoft.sleepforbreakfast.money.list

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.sleepforbreakfast.db.DbInsert

interface ListInteractor<I : Any, T : Any, CE : Any> {

  @CheckResult suspend fun loadOne(force: Boolean, id: I): ResultWrapper<T>

  @CheckResult suspend fun loadAll(force: Boolean): ResultWrapper<List<T>>

  @CheckResult suspend fun listenForItemChanges(onEvent: (CE) -> Unit)

  @CheckResult suspend fun submit(item: T): ResultWrapper<DbInsert.InsertResult<T>>

  @CheckResult suspend fun delete(item: T): ResultWrapper<Boolean>
}
