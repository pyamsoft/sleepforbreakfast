package com.pyamsoft.sleepforbreakfast.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.money.list.Search

@Composable
internal fun HeaderKnobs(
    modifier: Modifier,
    state: TransactionViewState,
    onSearchToggled: () -> Unit,
) {
  Row(
      modifier = modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End,
  ) {
    Search(
        state = state,
        onSearchToggled = onSearchToggled,
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
