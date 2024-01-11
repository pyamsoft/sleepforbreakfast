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

package com.pyamsoft.sleepforbreakfast.money.observer

import androidx.annotation.CheckResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import kotlinx.coroutines.CoroutineScope

interface BaseDbObserver<T : Any, I : Any> {

  fun bind(scope: CoroutineScope)

  @Composable @CheckResult fun collect(): SnapshotStateList<T>

  @Composable @CheckResult fun map(id: I): T

  @Composable @CheckResult fun map(ids: SnapshotStateList<I>): SnapshotStateList<T>
}
