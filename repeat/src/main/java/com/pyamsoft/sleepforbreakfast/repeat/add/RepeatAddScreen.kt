package com.pyamsoft.sleepforbreakfast.repeat.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.MoneyAmount
import com.pyamsoft.sleepforbreakfast.money.MoneyName
import com.pyamsoft.sleepforbreakfast.money.MoneyNote
import com.pyamsoft.sleepforbreakfast.money.MoneySubmit
import com.pyamsoft.sleepforbreakfast.money.MoneyType

@Composable
fun RepeatAddScreen(
    modifier: Modifier = Modifier,
    state: RepeatAddViewState,
    onNameChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onAmountChanged: (Long) -> Unit,
    onTypeChanged: (DbTransaction.Type) -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
  val keyboardNumberOptions = remember {
    KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Decimal,
        imeAction = ImeAction.Next,
    )
  }

  val keyboardTextOptions = remember {
    KeyboardOptions(
        capitalization = KeyboardCapitalization.Words,
        imeAction = ImeAction.Next,
    )
  }

  Column(
      modifier = modifier,
  ) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.primary,
        contentColor = MaterialTheme.colors.onPrimary,
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
        title = {},
    )

    LazyColumn {
      item {
        MoneyName(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            state = state,
            keyboardTextOptions = keyboardTextOptions,
            onNameChanged = onNameChanged,
        )
      }

      item {
        MoneyAmount(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = MaterialTheme.keylines.content)
                    .padding(bottom = MaterialTheme.keylines.content * 2),
            state = state,
            keyboardNumberOptions = keyboardNumberOptions,
            onAmountChanged = onAmountChanged,
        )
      }

      item {
        MoneyType(
            modifier =
                Modifier.fillMaxWidth()
                    .padding(horizontal = MaterialTheme.keylines.content)
                    .padding(bottom = MaterialTheme.keylines.content),
            state = state,
            onTypeChanged = onTypeChanged,
        )
      }

      item {
        MoneyNote(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            state = state,
            onNoteChanged = onNoteChanged,
            label = {
              Text(
                  text = "Note about this Repeating Transaction",
              )
            },
        )
      }

      item {
        MoneySubmit(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            state = state,
            onReset = onReset,
            onSubmit = onSubmit,
        )
      }
    }
  }
}
