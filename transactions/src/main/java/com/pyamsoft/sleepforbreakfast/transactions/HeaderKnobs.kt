package com.pyamsoft.sleepforbreakfast.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Popup
import com.pyamsoft.pydroid.theme.keylines

@Composable
internal fun HeaderKnobs(
    modifier: Modifier,
    state: TransactionViewState,
    onSearchToggled: () -> Unit,
    onSearchUpdated: (String) -> Unit,
) {
  Row(
      modifier = modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End,
  ) {
    Search(
        state = state,
        onSearchToggled = onSearchToggled,
        onSearchUpdated = onSearchUpdated,
    )

    IconButton(
        onClick = {},
    ) {
      Icon(
          imageVector = Icons.Filled.Build,
          contentDescription = "One",
      )
    }

    IconButton(
        onClick = {},
    ) {
      Icon(
          imageVector = Icons.Filled.Build,
          contentDescription = "One",
      )
    }
  }
}

@Composable
private fun Search(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    onSearchToggled: () -> Unit,
    onSearchUpdated: (String) -> Unit,
) {
  val isSearchOpen by state.isSearchOpen.collectAsState()
  val search by state.search.collectAsState()
  val hasSearch = remember(search) { search.isNotBlank() }

  IconButton(
      modifier = modifier,
      onClick = onSearchToggled,
  ) {
    Icon(
        imageVector = Icons.Filled.Search,
        contentDescription = "Search",
        tint =
            MaterialTheme.colors.onPrimary.copy(
                alpha = if (hasSearch) ContentAlpha.high else ContentAlpha.medium,
            ),
    )
  }

  if (isSearchOpen) {
    Popup {
      Text(
          text = "Hello!",
      )
    }
  }
}
