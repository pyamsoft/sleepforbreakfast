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

package com.pyamsoft.sleepforbreakfast.repeat.add

import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.money.MoneyViewState
import com.pyamsoft.sleepforbreakfast.money.MutableMoneyAddViewState
import java.time.Clock
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface RepeatAddViewState : MoneyViewState {
  val repeatFirstDay: StateFlow<LocalDate>
  val repeatType: StateFlow<DbRepeat.Type>
}

@Stable
class MutableRepeatAddViewState
@Inject
internal constructor(
    clock: Clock,
) : RepeatAddViewState, MutableMoneyAddViewState() {
  override val repeatFirstDay = MutableStateFlow(LocalDate.now(clock))
  override val repeatType = MutableStateFlow(DbRepeat.Type.DAILY)
}
