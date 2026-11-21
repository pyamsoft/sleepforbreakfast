/*
 * Copyright 2026 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.ui.text

import androidx.annotation.CheckResult
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.runtime.saveable.SaveableStateRegistry
import androidx.compose.runtime.saveable.SaverScope

/** Completely copy a TextFieldState data over to another */
fun TextFieldState.replaceWith(other: TextFieldState) {
  val self = this

  self.edit {
    replace(0, length, other.text)
    selection = other.selection

    // TODO(Peter): Can not replace the undoState :(
    // We'd need to make the field itself mutable
  }
}

/** Easy save text field state */
@CheckResult
fun TextFieldState.saveToRegistry(registry: SaveableStateRegistry): Any? {
  val self = this

  val scope = SaverScope { registry.canBeSaved(it) }
  return with(TextFieldState.Saver) {
    scope.save(self)
  }
}
