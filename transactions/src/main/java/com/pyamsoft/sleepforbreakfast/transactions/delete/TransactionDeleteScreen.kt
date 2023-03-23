package com.pyamsoft.sleepforbreakfast.transactions.delete

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.money.delete.DeleteScreen
import com.pyamsoft.sleepforbreakfast.transactions.TransactionCard

@Composable
fun TransactionDeleteScreen(
    modifier: Modifier = Modifier,
    state: TransactionDeleteViewState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
  DeleteScreen(
      modifier = modifier,
      state = state,
      onDismiss = onDismiss,
      onConfirm = onConfirm,
  ) { transaction ->
    TransactionCard(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.content),
        transaction = transaction,
    )
  }
}
