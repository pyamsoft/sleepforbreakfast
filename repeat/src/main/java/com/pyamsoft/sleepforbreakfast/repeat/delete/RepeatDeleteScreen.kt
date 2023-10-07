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

package com.pyamsoft.sleepforbreakfast.repeat.delete

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.money.category.CategoryIdMapper
import com.pyamsoft.sleepforbreakfast.money.delete.DeleteScreen
import com.pyamsoft.sleepforbreakfast.repeat.RepeatCard

@Composable
fun RepeatDeleteScreen(
    modifier: Modifier = Modifier,
    state: RepeatDeleteViewState,
    mapper: CategoryIdMapper,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
  DeleteScreen(
      modifier = modifier,
      state = state,
      onDismiss = onDismiss,
      onConfirm = onConfirm,
  ) { repeat ->
    Text(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
        text = "Are you sure you want to remove this repeating transaction?",
        style = MaterialTheme.typography.body1,
    )

    RepeatCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
        repeat = repeat,
        mapper = mapper,
    )
  }
}
