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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.pydroid.ui.defaults.ImageDefaults
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
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
    onOpenAllTransactions: () -> Unit,
    onOpenTransactions: (DbCategory) -> Unit,
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

    item(
        contentType = ContentTypes.HEADER,
    ) {
      HomeHeader(
          modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.content),
          appName = appName,
          onOpenSettings = onOpenSettings,
      )
    }

    renderPYDroidExtras()

    item(
        contentType = ContentTypes.OPTIONS,
    ) {
      HomeOptions(
          modifier = Modifier.fillMaxWidth(),
          state = state,
          appName = appName,
          onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
      )
    }

    item(
        contentType = ContentTypes.TRANSACTIONS,
    ) {
      HomeCategories(
          modifier = Modifier.fillMaxWidth(),
          state = state,
          onOpenAllTransactions = onOpenAllTransactions,
          onOpenCategory = onOpenTransactions,
      )
    }

    item(
        contentType = ContentTypes.EXTRAS,
    ) {
      HomeExtras(
          modifier = Modifier.fillMaxWidth(),
          onOpenRepeats = onOpenRepeats,
          onOpenCategories = onOpenCategories,
      )
    }
  }
}

@Composable
private fun HomeCategories(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    onOpenAllTransactions: () -> Unit,
    onOpenCategory: (DbCategory) -> Unit,
) {
  val categories = state.categories.collectAsStateListWithLifecycle()

  Column(
      modifier = modifier.padding(MaterialTheme.keylines.content),
  ) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
        onClick = onOpenAllTransactions,
    ) {
      Text(
          text = "View All Transactions",
      )
    }

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.baseline),
    ) {
      item {
        Category(
            category = DbCategory.NONE,
            onOpen = onOpenCategory,
        )
      }

      items(
          items = categories,
          key = { it.id.raw },
      ) { category ->
        Category(
            category = category,
            onOpen = onOpenCategory,
        )
      }
    }
  }
}

@Composable
private fun Category(
    modifier: Modifier = Modifier,
    category: DbCategory,
    onOpen: (DbCategory) -> Unit,
) {
  val defaultColor = MaterialTheme.colors.surface
  val color =
      remember(
          category,
          defaultColor,
      ) {
        if (category.color == 0L) defaultColor else Color(category.color.toULong())
      }

  val fontStyle =
      remember(
          category,
      ) {
        if (category.id.isEmpty) FontStyle.Italic else null
      }
  val defaultCategoryAlpha = ContentAlpha.high
  val noneCategoryAlpha = ContentAlpha.medium
  val contentColorAlpha =
      remember(
          category,
          noneCategoryAlpha,
          defaultCategoryAlpha,
      ) {
        if (category.id.isEmpty) noneCategoryAlpha else defaultCategoryAlpha
      }

  Card(
      modifier = modifier,
      elevation = CardDefaults.Elevation,
      backgroundColor = color,
  ) {
    Column(
        modifier = Modifier.clickable { onOpen(category) }.padding(MaterialTheme.keylines.content),
    ) {
      Text(
          style =
              MaterialTheme.typography.h6.copy(
                  color =
                      MaterialTheme.colors.onSurface.copy(
                          alpha = contentColorAlpha,
                      ),
              ),
          text = category.name,
          fontStyle = fontStyle,
      )
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
