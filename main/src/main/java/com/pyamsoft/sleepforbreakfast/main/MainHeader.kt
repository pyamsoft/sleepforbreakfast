package com.pyamsoft.sleepforbreakfast.main

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MainHeader(
    modifier: Modifier = Modifier,
    appName: String,
    scaffoldPaddingValues: PaddingValues,
    onSettingsOpen: () -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    Spacer(
        modifier = Modifier.padding(scaffoldPaddingValues).statusBarsPadding(),
    )
    Row(
        verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
          modifier = Modifier.weight(1F),
          text = appName,
          style = MaterialTheme.typography.h5,
      )
      IconButton(
          onClick = onSettingsOpen,
      ) {
        Icon(
            imageVector = Icons.Filled.Settings,
            contentDescription = "Settings",
        )
      }
    }
  }
}
