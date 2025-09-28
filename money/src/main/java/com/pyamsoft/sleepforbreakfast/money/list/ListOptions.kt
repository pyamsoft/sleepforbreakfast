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

package com.pyamsoft.sleepforbreakfast.money.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.ui.debouncedOnTextChange
import kotlinx.coroutines.flow.filter

@Composable
private fun UsageIndicator(
    modifier: Modifier = Modifier,
    show: Boolean,
) {
  AnimatedVisibility(
      visible = show,
      enter = scaleIn(),
      exit = scaleOut(),
  ) {
    Box(
        modifier =
            modifier
                .padding(bottom = MaterialTheme.keylines.baseline * 1.5F)
                .padding(end = MaterialTheme.keylines.baseline)
                .size(MaterialTheme.keylines.baseline)
                .background(
                    color = MaterialTheme.colorScheme.tertiary,
                    shape =
                        RoundedCornerShape(
                            percent = 50,
                        ),
                ),
    )
  }
}

@Composable
fun KnobBar(
    modifier: Modifier = Modifier,
    isOpen: Boolean,
    onToggle: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
  AnimatedVisibility(
      visible = isOpen,
      enter = fadeIn() + slideInHorizontally(),
      exit = fadeOut() + slideOutHorizontally(),
  ) {
    SwipeAway(
        onSwiped = onToggle,
    ) {
      Surface(
          modifier = modifier,
      ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            verticalAlignment = Alignment.CenterVertically,
        ) {
          content()

          IconButton(
              modifier = Modifier.padding(start = MaterialTheme.keylines.content),
              onClick = onToggle,
          ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun SwipeAway(
    modifier: Modifier = Modifier,
    onSwiped: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
  val handleSwiped by rememberUpdatedState(onSwiped)

  val swipeState = rememberSwipeToDismissBoxState()

  LaunchedEffect(swipeState) {
    snapshotFlow { swipeState.currentValue }
        .filter { it != SwipeToDismissBoxValue.Settled }
        .collect { handleSwiped() }
  }

  setOf(SwipeToDismissBoxValue.EndToStart, SwipeToDismissBoxValue.StartToEnd)
  SwipeToDismissBox(
      modifier = modifier,
      state = swipeState,
      backgroundContent = {},
      enableDismissFromStartToEnd = true,
      enableDismissFromEndToStart = true,
      content = content,
  )
}

@Composable
fun ToggleIcon(
    modifier: Modifier = Modifier,
    showUsage: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit,
) {
  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomEnd,
  ) {
    IconButton(
        onClick = onToggle,
    ) {
      content()
    }

    UsageIndicator(
        show = showUsage,
    )
  }
}

@Composable
fun Search(
    modifier: Modifier = Modifier,
    state: ListViewState<*>,
    onToggle: () -> Unit,
) {
  val search by state.search.collectAsStateWithLifecycle()
  val showUsage = remember(search) { search.isNotBlank() }

  ToggleIcon(
      modifier = modifier,
      showUsage = showUsage,
      onToggle = onToggle,
  ) {
    Icon(
        imageVector = Icons.Filled.Search,
        contentDescription = "Search",
        tint = MaterialTheme.colorScheme.onPrimary,
    )
  }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    state: ListViewState<*>,
    onToggle: () -> Unit,
    onChange: (String) -> Unit,
    leadingIcon: @Composable () -> Unit = {},
    trailingIcon: @Composable () -> Unit = {},
) {
  val initialSearch by state.search.collectAsStateWithLifecycle()
  val isOpen by state.isSearchOpen.collectAsStateWithLifecycle()

  // Do this so that we can debounce typing events
  val (search, setSearch) = debouncedOnTextChange(initialSearch, onChange)

  KnobBar(
      modifier = modifier,
      isOpen = isOpen,
      onToggle = onToggle,
  ) {
    TextField(
        modifier = Modifier.weight(1F),
        value = search,
        onValueChange = { setSearch(it) },
        keyboardOptions =
            remember { KeyboardOptions(autoCorrectEnabled = true, imeAction = ImeAction.Search) },
        leadingIcon = {
          Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = "Search",
          )
          leadingIcon()
        },
        trailingIcon = {
          Row(
              verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
                modifier = Modifier.clickable { setSearch("") },
                imageVector = Icons.Filled.Clear,
                contentDescription = "Clear",
            )

            // Extra content
            trailingIcon()
          }
        },
        label = {
          Text(
              text = "Search",
          )
        },
    )
  }
}
