package com.pyamsoft.sleepforbreakfast.sources.add

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.core.REGEX_FILTER_ONLY_DIGITS
import com.pyamsoft.sleepforbreakfast.money.add.AddName
import com.pyamsoft.sleepforbreakfast.money.add.AddNote
import com.pyamsoft.sleepforbreakfast.money.add.AddSubmit

@Composable
fun SourcesAddScreen(
    modifier: Modifier = Modifier,
    state: SourcesAddViewState,
    onNameChanged: (String) -> Unit,
    onNoteChanged: (String) -> Unit,
    onAccountNumberChanged: (String) -> Unit,
    onReset: () -> Unit,
    onSubmit: () -> Unit,
    onDismiss: () -> Unit,
) {
  val name by state.name.collectAsState()
  val note by state.note.collectAsState()
  val working by state.working.collectAsState()

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
        AddName(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            name = name,
            keyboardTextOptions = keyboardTextOptions,
            onNameChanged = onNameChanged,
        )
      }

      item {
        AccountNumber(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            state = state,
            onAccountNumberChanged = onAccountNumberChanged,
        )
      }

      item {
        AddNote(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            note = note,
            onNoteChanged = onNoteChanged,
            label = {
              Text(
                  text = "Note about this Source",
              )
            },
        )
      }

      item {
        AddSubmit(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            working = working,
            onReset = onReset,
            onSubmit = onSubmit,
        )
      }
    }
  }
}

@Composable
private fun AccountNumber(
    modifier: Modifier = Modifier,
    state: SourcesAddViewState,
    onAccountNumberChanged: (String) -> Unit,
) {
  val accountNumber by state.accountNumber.collectAsState()

  val keyboardNumberOptions = remember {
    KeyboardOptions(
        capitalization = KeyboardCapitalization.None,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next,
    )
  }

  val handleAccountChanged by rememberUpdatedState { text: String ->
    // Ignore anything not a number when parsing to number
    val nums = text.trim().replace(REGEX_FILTER_ONLY_DIGITS, "")
    onAccountNumberChanged(nums)
  }

  TextField(
      modifier = modifier,
      value = accountNumber,
      onValueChange = { handleAccountChanged(it) },
      keyboardOptions = keyboardNumberOptions,
      label = {
        Text(
            text = "Last 4 digits",
        )
      },
  )
}
