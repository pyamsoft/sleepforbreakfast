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

package com.pyamsoft.sleepforbreakfast.category.delete

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.category.CategoryCard
import com.pyamsoft.sleepforbreakfast.money.delete.DeleteScreen

@Composable
fun CategoryDeleteScreen(
    modifier: Modifier = Modifier,
    state: CategoryDeleteViewState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
  DeleteScreen(
      modifier = modifier,
      state = state,
      onDismiss = onDismiss,
      onConfirm = onConfirm,
  ) { category ->
    CategoryCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
        category = category,
    )
  }
}
