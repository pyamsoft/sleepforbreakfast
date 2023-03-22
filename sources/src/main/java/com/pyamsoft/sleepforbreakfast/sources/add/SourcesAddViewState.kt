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

package com.pyamsoft.sleepforbreakfast.sources.add

import androidx.compose.runtime.Stable
import com.pyamsoft.pydroid.arch.UiViewState
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
interface SourcesAddViewState : UiViewState {
  val name: StateFlow<String>
  val accountNumber: StateFlow<String>
  val note: StateFlow<String>
  val working: StateFlow<Boolean>
}

@Stable
class MutableSourcesAddViewState @Inject internal constructor() : SourcesAddViewState {
  override val name = MutableStateFlow("")
  override val accountNumber = MutableStateFlow("")
  override val note = MutableStateFlow("")
  override val working = MutableStateFlow(false)
}