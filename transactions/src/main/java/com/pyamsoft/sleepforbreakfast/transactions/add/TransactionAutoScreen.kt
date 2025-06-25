/*
 * Copyright 2025 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.ImageDefaults
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.money.DATE_FORMATTER
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
import java.time.LocalDate

private enum class AutoContentTypes {
  TITLE,
  MATCH_TEXT,
  AMOUNT,
  NOTIFICATION_ID,
  NOTIFICATION_PACKAGE,
  NOTIFICATION_KEY,
  NOTIFICATION_GROUP,
  CREATED_DATE,
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TransactionAutoScreen(
    modifier: Modifier = Modifier,
    loading: LoadingState,
    auto: DbAutomatic?,
    date: LocalDate,
    onDismiss: () -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    val contentColor = LocalContentColor.current

    TopAppBar(
        modifier = Modifier.fillMaxWidth(),
        colors =
            TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                actionIconContentColor = contentColor,
                navigationIconContentColor = contentColor,
                titleContentColor = contentColor,
            ),
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
              text = "Automatic Transaction",
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
            label = "Transaction Auto Info",
            targetState = auto,
        ) { a ->
          if (a == null) {
            Box(
                modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
                contentAlignment = Alignment.Center,
            ) {
              Text(
                  modifier = Modifier.fillMaxWidth(),
                  text = "An unexpected error occurred, please try again later.",
                  textAlign = TextAlign.Center,
                  color = MaterialTheme.colorScheme.error,
                  style = MaterialTheme.typography.bodyLarge,
              )
            }
          } else {
            LazyColumn {
              item(
                  contentType = AutoContentTypes.TITLE,
              ) {
                Text(
                    modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
                    text = "Notification: ${a.notificationTitle}",
                    style = MaterialTheme.typography.bodyLarge,
                )
              }

              item(
                  contentType = AutoContentTypes.MATCH_TEXT,
              ) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.keylines.content)
                            .padding(bottom = MaterialTheme.keylines.content),
                    text = "Matched: ${a.notificationMatchText}",
                    style = MaterialTheme.typography.bodyLarge,
                )
              }

              item(
                  contentType = AutoContentTypes.AMOUNT,
              ) {
                val money =
                    remember(a.notificationAmountInCents) {
                      MoneyVisualTransformation.format(a.notificationAmountInCents)
                    }
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.keylines.content)
                            .padding(bottom = MaterialTheme.keylines.content * 2),
                    text = "Amount: $money",
                    style = MaterialTheme.typography.bodyLarge,
                )
              }

              item(
                  contentType = AutoContentTypes.NOTIFICATION_ID,
              ) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.keylines.content)
                            .padding(bottom = MaterialTheme.keylines.content),
                    text = "ID: ${a.notificationId}",
                    style = MaterialTheme.typography.bodyLarge,
                )
              }

              item(
                  contentType = AutoContentTypes.NOTIFICATION_PACKAGE,
              ) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.keylines.content)
                            .padding(bottom = MaterialTheme.keylines.content),
                    text = "Package: ${a.notificationPackageName}",
                    style = MaterialTheme.typography.bodyLarge,
                )
              }

              item(
                  contentType = AutoContentTypes.NOTIFICATION_KEY,
              ) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.keylines.content)
                            .padding(bottom = MaterialTheme.keylines.content),
                    text = "Key: ${a.notificationKey}",
                    style = MaterialTheme.typography.bodyLarge,
                )
              }

              item(
                  contentType = AutoContentTypes.NOTIFICATION_GROUP,
              ) {
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.keylines.content)
                            .padding(bottom = MaterialTheme.keylines.content),
                    text = "Group: ${a.notificationGroup}",
                    style = MaterialTheme.typography.bodyLarge,
                )
              }

              item(
                  contentType = AutoContentTypes.CREATED_DATE,
              ) {
                val dateString = remember(date) { DATE_FORMATTER.format(date) }
                Text(
                    modifier =
                        Modifier.fillMaxWidth()
                            .padding(horizontal = MaterialTheme.keylines.content)
                            .padding(bottom = MaterialTheme.keylines.content),
                    text = "Created On: $dateString",
                    style = MaterialTheme.typography.bodyLarge,
                )
              }
            }
          }
        }
      }
    }
  }
}
