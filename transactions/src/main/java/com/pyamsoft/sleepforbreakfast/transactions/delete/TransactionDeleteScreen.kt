package com.pyamsoft.sleepforbreakfast.transactions.delete

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.transactions.TransactionCard

@Composable
fun TransactionDeleteScreen(
    modifier: Modifier = Modifier,
    state: TransactionDeleteViewState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
  val working by state.working.collectAsState()
  val transaction by state.transaction.collectAsState()

  Column(
      modifier = modifier,
  ) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
        title = {
          Text(
              text = "Really Remove?",
          )
        },
        navigationIcon = {
          IconButton(
              onClick = onDismiss,
          ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Close",
            )
          }
        },
    )

    Crossfade(
        modifier = Modifier.padding(MaterialTheme.keylines.content),
        targetState = transaction,
    ) { t ->
      if (t == null) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator()
        }
      } else {
        Column {
          Text(
              modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
              text = "Are you sure you want to remove this transaction?")

          TransactionCard(
              modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
              transaction = t,
          )

          Button(
              modifier = Modifier.fillMaxWidth(),
              onClick = onConfirm,
              enabled = !working,
          ) {
            Crossfade(
                targetState = working,
            ) { w ->
              if (w) {
                CircularProgressIndicator()
              } else {
                Text(
                    text = "Remove",
                )
              }
            }
          }
        }
      }
    }
  }
}
