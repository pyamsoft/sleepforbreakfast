package com.pyamsoft.sleepforbreakfast.money.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.sleepforbreakfast.ui.debouncedOnTextChange

@Composable
fun Search(
    modifier: Modifier = Modifier,
    state: ListViewState<*>,
    onToggle: () -> Unit,
) {
  val isOpen by state.isSearchOpen.collectAsState()

  IconButton(
      modifier = modifier,
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
