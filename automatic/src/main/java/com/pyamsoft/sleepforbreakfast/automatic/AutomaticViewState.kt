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

package com.pyamsoft.sleepforbreakfast.automatic

import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.automatic.add.AutomaticAddParams
import com.pyamsoft.sleepforbreakfast.automatic.delete.AutomaticDeleteParams
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.money.list.ListViewState
import com.pyamsoft.sleepforbreakfast.money.list.MutableListViewState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface AutomaticViewState : ListViewState<DbNotificationWithRegexes> {
  val addParams: StateFlow<AutomaticAddParams?>
  val deleteParams: StateFlow<AutomaticDeleteParams?>
}

@Stable
class MutableAutomaticViewState @Inject internal constructor() :
    AutomaticViewState, MutableListViewState<DbNotificationWithRegexes>() {
  override val addParams = MutableStateFlow<AutomaticAddParams?>(null)
  override val deleteParams = MutableStateFlow<AutomaticDeleteParams?>(null)
}
