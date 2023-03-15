package com.pyamsoft.sleepforbreakfast.home

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.DialogDefaults

@Composable
internal fun HomeOpenTransactions(
    modifier: Modifier = Modifier,
    onOpenTransactions: () -> Unit,
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
          modifier =
              Modifier.clickable { onOpenTransactions() }.padding(MaterialTheme.keylines.content),
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
