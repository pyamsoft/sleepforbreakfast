package com.pyamsoft.sleepforbreakfast.money.list

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import kotlinx.coroutines.delay

@Composable
fun Search(
    modifier: Modifier = Modifier,
    state: ListViewState<*>,
    onSearchToggled: () -> Unit,
) {
  val isOpen by state.isSearchOpen.collectAsState()

  IconButton(
      modifier = modifier,
      onClick = onSearchToggled,
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
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    state: ListViewState<*>,
    onSearchToggled: () -> Unit,
    onSearchUpdated: (String) -> Unit,
) {
  val initialSearch by state.search.collectAsState()
  val isOpen by state.isSearchOpen.collectAsState()

  // Do this so that we can debounce typing events
  val (search, setSearch) = remember { mutableStateOf(initialSearch) }
  val handleSearchUpdated by rememberUpdatedState(onSearchUpdated)
  LaunchedEffect(search) {
    delay(300L)
    handleSearchUpdated(search)
  }

  BackHandler(
      enabled = isOpen,
      onBack = onSearchToggled,
  )

  AnimatedVisibility(
      visible = isOpen,
      enter = fadeIn() + slideInHorizontally(),
      exit = fadeOut() + slideOutHorizontally(),
  ) {
    Surface(
        modifier = modifier,
        elevation = CardDefaults.Elevation,
    ) {
      Box(
          modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
          contentAlignment = Alignment.Center,
      ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
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
  }
}
