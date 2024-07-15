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

package com.pyamsoft.sleepforbreakfast.main.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.settings.SettingsPage
import com.pyamsoft.sleepforbreakfast.ui.CardDialog

@Composable
fun SettingsDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
  CardDialog(
      modifier = modifier,
      onDismiss = onDismiss,
  ) {
    SettingsToolbar(
        modifier = Modifier.fillMaxWidth(),
        onClose = onDismiss,
    )
    SettingsPage(
        modifier = Modifier.fillMaxWidth().weight(1F),
        customBottomItemMargin = MaterialTheme.keylines.baseline,
    )
  }
}
