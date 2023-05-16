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

package com.pyamsoft.sleepforbreakfast.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults
import com.pyamsoft.pydroid.ui.defaults.ImageDefaults
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.sleepforbreakfast.ui.icons.Category
import com.pyamsoft.sleepforbreakfast.ui.icons.EventRepeat
import com.pyamsoft.sleepforbreakfast.ui.renderPYDroidExtras

private enum class ContentTypes {
  SPACER,
  HEADER,
  OPTIONS,
  TRANSACTIONS,
  EXTRAS,
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    appName: String,
    onOpenSettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenTransactions: () -> Unit,
    onOpenRepeats: () -> Unit,
    onOpenCategories: () -> Unit,
) {
  LazyColumn(
      modifier = modifier,
  ) {
    item(
        contentType = ContentTypes.SPACER,
    ) {
      Spacer(
          modifier = Modifier.statusBarsPadding(),
      )
    }

    renderPYDroidExtras()

    item(
        contentType = ContentTypes.HEADER,
    ) {
      HomeHeader(
          modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.content),
          appName = appName,
          onOpenSettings = onOpenSettings,
      )
    }

    item(
        contentType = ContentTypes.OPTIONS,
    ) {
      HomeOptions(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
          state = state,
          onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
      )
    }

    item(
        contentType = ContentTypes.TRANSACTIONS,
    ) {
      HomeOpenTransactions(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
          onOpen = onOpenTransactions,
      )
    }

    item(
        contentType = ContentTypes.EXTRAS,
    ) {
      HomeExtras(
          modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
          onOpenRepeats = onOpenRepeats,
          onOpenCategories = onOpenCategories,
      )
    }
  }
}

@Composable
private fun HomeOpenTransactions(
    modifier: Modifier = Modifier,
    onOpen: () -> Unit,
) {
  val shape = MaterialTheme.shapes.medium

  Box(
      modifier = modifier.padding(MaterialTheme.keylines.content),
  ) {
    Surface(
        modifier =
            Modifier.fillMaxWidth()
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colors.primary,
                    shape = shape,
                ),
        shape = shape,
        elevation = DialogDefaults.Elevation,
    ) {
      Column(
          modifier = Modifier.clickable { onOpen() }.padding(MaterialTheme.keylines.content),
      ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
              text = "Manage Transactions",
              style =
                  MaterialTheme.typography.h6.copy(
                      fontWeight = FontWeight.W700,
                  ),
          )
        }
      }
    }
  }
}

@Composable
private fun HomeExtras(
    modifier: Modifier = Modifier,
    onOpenRepeats: () -> Unit,
    onOpenCategories: () -> Unit
) {
  Row(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    IconOption(
        modifier = Modifier.weight(1F),
        onClick = onOpenCategories,
        icon = Icons.Filled.Category,
        title = "View Categories",
    )

    Spacer(
        modifier = Modifier.width(MaterialTheme.keylines.content),
    )

    IconOption(
        modifier = Modifier.weight(1F),
        onClick = onOpenRepeats,
        icon = Icons.Filled.EventRepeat,
        title = "View Repeats",
    )
  }
}

@Composable
private fun IconOption(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    title: String,
) {
  val shape = MaterialTheme.shapes.medium

  Surface(
      modifier =
          modifier.border(
              width = 2.dp,
              color = MaterialTheme.colors.primary,
              shape = shape,
          ),
      shape = shape,
      elevation = ZeroElevation,
      color =
          MaterialTheme.colors.primary.copy(
              alpha = 0.20F,
          ),
      contentColor = MaterialTheme.colors.onPrimary,
  ) {
    Column(
        modifier = Modifier.clickable { onClick() }.padding(MaterialTheme.keylines.content),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
          modifier =
              Modifier.size(ImageDefaults.LargeSize)
                  .padding(bottom = MaterialTheme.keylines.content),
          imageVector = icon,
          contentDescription = title,
          tint = MaterialTheme.colors.onPrimary,
      )
      Text(
          text = title,
          style =
              MaterialTheme.typography.body1.copy(
                  color =
                      MaterialTheme.colors.onPrimary.copy(
                          alpha = ContentAlpha.medium,
                      )),
      )
    }
  }
}
