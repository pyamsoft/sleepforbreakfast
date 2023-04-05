package com.pyamsoft.sleepforbreakfast.money.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
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
import androidx.compose.material.ContentAlpha
import androidx.compose.material.DismissValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.SwipeToDismiss
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberDismissState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.theme.success
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.sleepforbreakfast.ui.debouncedOnTextChange
import kotlinx.coroutines.flow.filter

@Composable
fun Search(
    modifier: Modifier = Modifier,
    state: ListViewState<*>,
    onToggle: () -> Unit,
) {
  val search by state.search.collectAsState()
  val isOpen by state.isSearchOpen.collectAsState()

  val show = remember(search) { search.isNotBlank() }

  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomEnd,
  ) {
    IconButton(
        onClick = onToggle,
    ) {
      Icon(
          imageVector = Icons.Filled.Search,
          contentDescription = "Search",
          tint =
              MaterialTheme.colors.onPrimary.copy(
                  alpha = if (isOpen) ContentAlpha.high else ContentAlpha.medium,
              ),
      )
    }

    UsageIndicator(
        show = show,
    )
  }
}

@Composable
@OptIn(ExperimentalAnimationApi::class)
fun UsageIndicator(
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
                    color = MaterialTheme.colors.success,
                    shape =
                        RoundedCornerShape(
                            percent = 50,
                        ),
                ),
    )
  }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    state: ListViewState<*>,
    onToggle: () -> Unit,
    onChange: (String) -> Unit,
) {
  val initialSearch by state.search.collectAsState()
  val isOpen by state.isSearchOpen.collectAsState()

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
            remember {
              KeyboardOptions(
                  autoCorrect = true,
                  imeAction = ImeAction.Search,
              )
            },
        leadingIcon = {
          Icon(
              imageVector = Icons.Filled.Search,
              contentDescription = "Search",
          )
        },
        trailingIcon = {
          Icon(
              modifier = Modifier.clickable { setSearch("") },
              imageVector = Icons.Filled.Clear,
              contentDescription = "Clear",
          )
        },
        label = {
          Text(
              text = "Search",
          )
        },
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
          elevation = CardDefaults.Elevation,
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
                tint = MaterialTheme.colors.error,
            )
          }
        }
      }
    }
  }
}

@Composable
@OptIn(ExperimentalMaterialApi::class)
private fun SwipeAway(
    modifier: Modifier = Modifier,
    onSwiped: () -> Unit,
    content: @Composable RowScope.() -> Unit,
) {
  val handleSwiped by rememberUpdatedState(onSwiped)

  val swipeState = rememberDismissState()

  LaunchedEffect(swipeState) {
    snapshotFlow { swipeState.currentValue }
        .filter { it != DismissValue.Default }
        .collect { handleSwiped() }
  }

  SwipeToDismiss(
      modifier = modifier,
      state = swipeState,
      background = {},
      dismissContent = content,
  )
}
