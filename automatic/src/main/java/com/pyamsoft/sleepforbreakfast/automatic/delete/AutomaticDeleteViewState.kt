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

package com.pyamsoft.sleepforbreakfast.automatic.delete

import androidx.compose.runtime.Stable
import com.pyamsoft.sleepforbreakfast.db.notification.DbNotificationWithRegexes
import com.pyamsoft.sleepforbreakfast.money.delete.DeleteViewState
import com.pyamsoft.sleepforbreakfast.money.delete.MutableDeleteViewState
import javax.inject.Inject

@Stable interface AutomaticDeleteViewState : DeleteViewState<DbNotificationWithRegexes>

@Stable
class MutableAutomaticDeleteViewState @Inject internal constructor() :
    AutomaticDeleteViewState, MutableDeleteViewState<DbNotificationWithRegexes>()
