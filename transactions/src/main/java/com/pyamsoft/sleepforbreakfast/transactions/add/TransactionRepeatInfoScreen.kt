/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.transactions.add

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.pyamsoft.pydroid.core.requireNotNull
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.ImageDefaults
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.money.DATE_FORMATTER
import com.pyamsoft.sleepforbreakfast.transactions.LocalCategoryColor
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import java.time.LocalDate

private enum class RepeatContentTypes {
  TITLE,
  TYPE,
  CREATED_DATE,
}

@Composable
fun TransactionRepeatInfoScreen(
    modifier: Modifier = Modifier,
    loading: LoadingState,
    repeat: DbRepeat?,
    date: LocalDate,
    onDismiss: () -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = LocalCategoryColor.current,
        contentColor = MaterialTheme.colors.onPrimary,
        elevation = ZeroElevation,
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
        title = {
          Text(
              text = "Repeat Source",
          )
        },
    )

    when (loading) {
      LoadingState.NONE,
      LoadingState.LOADING -> {
        Box(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator(
              modifier = Modifier.size(ImageDefaults.LargeSize),
          )
        }
      }
      LoadingState.DONE -> {
        Crossfade(
            label = "Transaction Repeat Info",
            targetState = repeat,
        ) { r ->
          if (r == null) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
                contentAlignment = Alignment.Center,
            ) {
              Text(
                  modifier = Modifier.fillMaxWidth(),
                  text = "An unexpected error occurred, please try again later.",
                  textAlign = TextAlign.Center,
                  color = MaterialTheme.colors.error,
                  style = MaterialTheme.typography.body1,
              )
            }
          } else {
            LazyColumn {
              item(
                  contentType = RepeatContentTypes.TITLE,
              ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
                    text = "Name: ${r.transactionName}",
                    style = MaterialTheme.typography.body1,
                )
              }

              item(
                  contentType = RepeatContentTypes.TYPE,
              ) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.keylines.content)
                            .padding(bottom = MaterialTheme.keylines.content),
                    text = "Type: ${r.repeatType.displayName}",
                    style = MaterialTheme.typography.body1,
                )
              }

              item(
                  contentType = RepeatContentTypes.CREATED_DATE,
              ) {
                val dateString =
                    remember(date) { DATE_FORMATTER.get().requireNotNull().format(date) }
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.keylines.content)
                            .padding(bottom = MaterialTheme.keylines.content),
                    text = "Created On: $dateString",
                    style = MaterialTheme.typography.body1,
                )
              }
            }
          }
        }
      }
    }
  }
}
