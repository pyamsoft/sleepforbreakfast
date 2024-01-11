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

package com.pyamsoft.sleepforbreakfast.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.uri.ExternalUriPortal
import com.pyamsoft.pydroid.ui.widget.BillingUpsellWidget
import com.pyamsoft.pydroid.ui.widget.NewVersionWidget
import com.pyamsoft.pydroid.ui.widget.ShowChangeLogWidget
import com.pyamsoft.pydroid.ui.widget.ShowDataPolicyDialog
import com.pyamsoft.pydroid.ui.widget.UpdateProgressWidget

private enum class PYDroidContentTypes {
  UPDATE_VERSION,
  NEW_VERSION,
  CHANGELOG,
  BILLING,
}

@Composable
fun InstallPYDroidExtras(
    modifier: Modifier = Modifier,
    appName: String,
) {
  ShowDataPolicyDialog()
  ExternalUriPortal(
      modifier = modifier,
      appName = appName,
  )
}

fun LazyListScope.renderPYDroidExtras(modifier: Modifier = Modifier) {
  item(
      contentType = PYDroidContentTypes.UPDATE_VERSION,
  ) {
    UpdateProgressWidget(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.keylines.content)
                .padding(top = MaterialTheme.keylines.content),
    )
  }

  item(
      contentType = PYDroidContentTypes.NEW_VERSION,
  ) {
    NewVersionWidget(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.keylines.content)
                .padding(top = MaterialTheme.keylines.content),
    )
  }

  item(
      contentType = PYDroidContentTypes.CHANGELOG,
  ) {
    ShowChangeLogWidget(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.keylines.content)
                .padding(top = MaterialTheme.keylines.content),
    )
  }

  item(
      contentType = PYDroidContentTypes.BILLING,
  ) {
    BillingUpsellWidget(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.keylines.content)
                .padding(top = MaterialTheme.keylines.content),
    )
  }
}
