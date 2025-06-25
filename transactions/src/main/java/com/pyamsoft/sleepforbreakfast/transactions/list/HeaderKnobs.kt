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

package com.pyamsoft.sleepforbreakfast.transactions.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.sleepforbreakfast.money.list.Search
import com.pyamsoft.sleepforbreakfast.money.list.ToggleIcon
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewState

@Composable
internal fun HeaderKnobs(
    modifier: Modifier,
    state: TransactionViewState,
    onSearchToggle: () -> Unit,
    onDateRangeToggle: () -> Unit,
) {
  Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.End,
  ) {
    DateRange(
        state = state,
        onToggle = onDateRangeToggle,
    )

    Search(
        state = state,
        onToggle = onSearchToggle,
    )
  }
}

@Composable
private fun DateRange(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    onToggle: () -> Unit,
) {
  val dateRange by state.dateRange.collectAsStateWithLifecycle()
  val showUsage = remember(dateRange) { dateRange != null }

  ToggleIcon(
      modifier = modifier,
      showUsage = showUsage,
      onToggle = onToggle,
  ) {
    Icon(
        imageVector = Icons.Filled.CalendarMonth,
        contentDescription = "Date Range",
        tint = MaterialTheme.colorScheme.onPrimary,
    )
  }
}
