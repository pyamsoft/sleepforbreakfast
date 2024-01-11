/*
 * Copyright 2024 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.home

import androidx.annotation.CheckResult
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Card
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.pydroid.ui.defaults.CardDefaults
import com.pyamsoft.pydroid.ui.defaults.ImageDefaults
import com.pyamsoft.pydroid.ui.theme.ZeroElevation
import com.pyamsoft.pydroid.ui.util.collectAsStateListWithLifecycle
import com.pyamsoft.pydroid.ui.util.collectAsStateMapWithLifecycle
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.SpendDirection
import com.pyamsoft.sleepforbreakfast.money.calculateTotalTransactionAmount
import com.pyamsoft.sleepforbreakfast.money.calculateTotalTransactionDirection
import com.pyamsoft.sleepforbreakfast.ui.COLOR_EARN
import com.pyamsoft.sleepforbreakfast.ui.COLOR_SPEND
import com.pyamsoft.sleepforbreakfast.ui.LoadingState
import com.pyamsoft.sleepforbreakfast.ui.icons.Category
import com.pyamsoft.sleepforbreakfast.ui.model.TransactionDateRange
import com.pyamsoft.sleepforbreakfast.ui.model.toDateRange
import com.pyamsoft.sleepforbreakfast.ui.rememberCurrentLocale
import com.pyamsoft.sleepforbreakfast.ui.renderPYDroidExtras
import com.pyamsoft.sleepforbreakfast.ui.text.MoneyVisualTransformation
import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import kotlin.math.abs

private enum class ContentTypes {
  SPACER,
  HEADER,
  OPTIONS,
  TRANSACTIONS,
  EXTRAS,
}

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    clock: Clock,
    state: HomeViewState,
    appName: String,
    onOpenSettings: () -> Unit,
    onOpenNotificationListenerSettings: () -> Unit,
    onOpenAllTransactions: (TransactionDateRange?) -> Unit,
    onOpenTransactions: (DbCategory, TransactionDateRange?) -> Unit,
    onOpenCategories: () -> Unit,
) {
  LazyColumn(
      modifier = modifier,
  ) {
    item(
        contentType = ContentTypes.SPACER,
    ) {
      Spacer(
          modifier = Modifier.statusBarsPadding(),
      )
    }

    item(
        contentType = ContentTypes.HEADER,
    ) {
      HomeHeader(
          modifier = Modifier.fillMaxWidth().padding(horizontal = MaterialTheme.keylines.content),
          appName = appName,
          onOpenSettings = onOpenSettings,
      )
    }

    renderPYDroidExtras()

    item(
        contentType = ContentTypes.OPTIONS,
    ) {
      HomeOptions(
          modifier = Modifier.fillMaxWidth(),
          state = state,
          appName = appName,
          onOpenNotificationListenerSettings = onOpenNotificationListenerSettings,
      )
    }

    item(
        contentType = ContentTypes.TRANSACTIONS,
    ) {
      HomeCategories(
          modifier = Modifier.fillMaxWidth(),
          clock = clock,
          state = state,
          onOpenAllTransactions = { onOpenAllTransactions(null) },
          onOpenCategory = { onOpenTransactions(it, null) },
          onOpenBreakdown = onOpenAllTransactions,
      )
    }

    item(
        contentType = ContentTypes.EXTRAS,
    ) {
      HomeExtras(
          modifier = Modifier.fillMaxWidth(),
          onOpenCategories = onOpenCategories,
      )
    }
  }
}

@Composable
@CheckResult
private fun rememberCategories(
    map: SnapshotStateMap<DbCategory.Id, Set<DbTransaction>>,
    id: DbCategory.Id
): Set<DbTransaction> {
  return remember(map, id) { map.getOrElse(id) { emptySet() } }
}

@Composable
@CheckResult
private fun rememberRange(
    map: SnapshotStateMap<HomeViewState.DayRange, Set<DbTransaction>>,
    range: HomeViewState.DayRange,
): Set<DbTransaction> {
  return remember(map, range) { map.getOrElse(range) { emptySet() } }
}

@Composable
private fun HomeCategories(
    modifier: Modifier = Modifier,
    state: HomeViewState,
    clock: Clock,
    onOpenAllTransactions: () -> Unit,
    onOpenCategory: (DbCategory) -> Unit,
    onOpenBreakdown: (TransactionDateRange) -> Unit,
) {
  val loading by state.loading.collectAsStateWithLifecycle()
  val categories = state.categories.collectAsStateListWithLifecycle()
  val transactionsByCategory = state.transactionsByCategory.collectAsStateMapWithLifecycle()
  val transactionsByRange = state.transactionsByDateRange.collectAsStateMapWithLifecycle()

  Column(
      modifier = modifier.padding(MaterialTheme.keylines.content),
  ) {
    OutlinedButton(
        modifier = Modifier.fillMaxWidth().padding(bottom = MaterialTheme.keylines.typography),
        onClick = onOpenAllTransactions,
    ) {
      Text(
          text = "View All Transactions",
      )
    }

    Crossfade(
        modifier = Modifier.fillMaxWidth(),
        label = "Loading",
        targetState = loading,
    ) { load ->
      if (load == LoadingState.DONE) {
        Column(
            modifier = Modifier.fillMaxWidth(),
        ) {
          LazyRow(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.baseline),
          ) {
            item {
              DateBreakdown(
                  transactions = rememberRange(transactionsByRange, HomeViewState.DayRange.DAY),
                  clock = clock,
                  type = DateBreakdownType.DAILY,
                  onOpen = onOpenBreakdown,
              )
            }

            item {
              DateBreakdown(
                  transactions = rememberRange(transactionsByRange, HomeViewState.DayRange.WEEK),
                  clock = clock,
                  type = DateBreakdownType.WEEKLY,
                  onOpen = onOpenBreakdown,
              )
            }

            item {
              DateBreakdown(
                  transactions = rememberRange(transactionsByRange, HomeViewState.DayRange.MONTH),
                  clock = clock,
                  type = DateBreakdownType.MONTHLY,
                  onOpen = onOpenBreakdown,
              )
            }
          }

          LazyRow(
              modifier = Modifier.fillMaxWidth().padding(top = MaterialTheme.keylines.content),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(MaterialTheme.keylines.baseline),
          ) {
            item {
              val uncategorizedTransactions =
                  rememberCategories(
                      transactionsByCategory,
                      DbCategory.Id.EMPTY,
                  )
              Category(
                  category = DbCategory.NONE,
                  onOpen = onOpenCategory,
                  transactions = uncategorizedTransactions,
              )
            }

            items(
                items = categories,
                key = { it.id.raw },
            ) { category ->
              val transactions =
                  rememberCategories(
                      transactionsByCategory,
                      category.id,
                  )
              Category(
                  category = category,
                  onOpen = onOpenCategory,
                  transactions = transactions,
              )
            }
          }
        }
      } else {
        Box(
            modifier = Modifier.fillMaxWidth().padding(MaterialTheme.keylines.content),
            contentAlignment = Alignment.Center,
        ) {
          CircularProgressIndicator(
              modifier = Modifier.size(80.dp),
          )
        }
      }
    }
  }
}

enum class DateBreakdownType {
  DAILY,
  WEEKLY,
  MONTHLY,
}

@Composable
private fun DateBreakdown(
    modifier: Modifier = Modifier,
    transactions: Set<DbTransaction>,
    clock: Clock,
    type: DateBreakdownType,
    onOpen: (TransactionDateRange) -> Unit,
) {
  val totalAmount = remember(transactions) { transactions.calculateTotalTransactionAmount() }
  val totalDirection = remember(totalAmount) { totalAmount.calculateTotalTransactionDirection() }
  val totalPrice =
      remember(
          transactions,
          totalAmount,
      ) {
        if (transactions.isEmpty()) "$0.00" else MoneyVisualTransformation.format(abs(totalAmount))
      }

  val zeroPriceColor = MaterialTheme.colors.onPrimary
  val priceColor =
      remember(
          totalDirection,
          zeroPriceColor,
      ) {
        when (totalDirection) {
          SpendDirection.NONE -> zeroPriceColor
          SpendDirection.SPEND -> COLOR_SPEND
          SpendDirection.EARN -> COLOR_EARN
        }
      }

  val today = remember(clock) { LocalDate.now(clock) }

  val rangeName =
      remember(type) {
        when (type) {
          DateBreakdownType.DAILY -> "Transcations Today"
          DateBreakdownType.WEEKLY -> "Transcations This Week"
          DateBreakdownType.MONTHLY -> "Transactions This Month"
        }
      }

  val locale = rememberCurrentLocale()
  val startOfRange =
      remember(
          type,
          clock,
          today,
          locale,
      ) {
        when (type) {
          DateBreakdownType.DAILY -> LocalDate.now(clock)
          DateBreakdownType.WEEKLY -> {
            val firstDayOfWeek = WeekFields.of(locale).firstDayOfWeek
            today.with(TemporalAdjusters.previousOrSame(firstDayOfWeek))
          }
          DateBreakdownType.MONTHLY -> today.withDayOfMonth(1)
        }
      }

  val dateRange =
      remember(
          startOfRange,
          today,
      ) {
        startOfRange.toDateRange(today)
      }

  val dateRangeFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT) }

  Card(
      modifier = modifier,
      elevation = CardDefaults.Elevation,
  ) {
    Column(
        modifier = Modifier.clickable { onOpen(dateRange) }.padding(MaterialTheme.keylines.content),
    ) {
      Text(
          style = MaterialTheme.typography.h6,
          text = rangeName,
      )

      Text(
          style = MaterialTheme.typography.caption,
          text =
              remember(
                  today,
                  type,
                  dateRange,
                  dateRangeFormatter,
              ) {
                if (type == DateBreakdownType.DAILY) {
                  return@remember dateRangeFormatter.format(today)
                } else {
                  val start = dateRangeFormatter.format(dateRange.from)
                  val end = dateRangeFormatter.format(dateRange.to)
                  return@remember "$start - $end"
                }
              },
      )

      Text(
          modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
          style =
              MaterialTheme.typography.body1.copy(
                  color =
                      priceColor.copy(
                          alpha = ContentAlpha.disabled,
                      ),
              ),
          text = totalPrice,
      )
    }
  }
}

@Composable
private fun Category(
    modifier: Modifier = Modifier,
    category: DbCategory,
    transactions: Set<DbTransaction>,
    onOpen: (DbCategory) -> Unit,
) {
  val defaultColor = MaterialTheme.colors.surface
  val color =
      remember(
          category,
          defaultColor,
      ) {
        if (category.color == 0L) defaultColor else Color(category.color.toULong())
      }

  val fontStyle =
      remember(
          category,
      ) {
        if (category.id.isEmpty) FontStyle.Italic else null
      }
  val defaultCategoryAlpha = ContentAlpha.high
  val noneCategoryAlpha = ContentAlpha.medium
  val contentColorAlpha =
      remember(
          category,
          noneCategoryAlpha,
          defaultCategoryAlpha,
      ) {
        if (category.id.isEmpty) noneCategoryAlpha else defaultCategoryAlpha
      }

  val totalAmount = remember(transactions) { transactions.calculateTotalTransactionAmount() }
  val totalDirection = remember(totalAmount) { totalAmount.calculateTotalTransactionDirection() }
  val totalPrice =
      remember(
          transactions,
          totalAmount,
      ) {
        if (transactions.isEmpty()) "$0.00" else MoneyVisualTransformation.format(abs(totalAmount))
      }

  val zeroPriceColor = MaterialTheme.colors.onPrimary
  val priceColor =
      remember(
          totalDirection,
          zeroPriceColor,
      ) {
        when (totalDirection) {
          SpendDirection.NONE -> zeroPriceColor
          SpendDirection.SPEND -> COLOR_SPEND
          SpendDirection.EARN -> COLOR_EARN
        }
      }

  Card(
      modifier = modifier,
      elevation = CardDefaults.Elevation,
      backgroundColor = color,
  ) {
    Column(
        modifier = Modifier.clickable { onOpen(category) }.padding(MaterialTheme.keylines.content),
    ) {
      Text(
          style =
              MaterialTheme.typography.h6.copy(
                  color =
                      MaterialTheme.colors.onSurface.copy(
                          alpha = contentColorAlpha,
                      ),
              ),
          text = category.name,
          fontStyle = fontStyle,
      )

      Text(
          modifier = Modifier.padding(top = MaterialTheme.keylines.baseline),
          style =
              MaterialTheme.typography.body1.copy(
                  color =
                      priceColor.copy(
                          alpha = ContentAlpha.disabled,
                      ),
              ),
          text = totalPrice,
      )
    }
  }
}

@Composable
private fun HomeExtras(modifier: Modifier = Modifier, onOpenCategories: () -> Unit) {
  Row(
      modifier = modifier.padding(MaterialTheme.keylines.content),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    IconOption(
        modifier = Modifier.weight(1F),
        onClick = onOpenCategories,
        icon = Icons.Filled.Category,
        title = "View Categories",
    )
  }
}

@Composable
private fun IconOption(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    icon: ImageVector,
    title: String,
) {
  val shape = MaterialTheme.shapes.medium

  Surface(
      modifier =
          modifier.border(
              width = 2.dp,
              color = MaterialTheme.colors.primary,
              shape = shape,
          ),
      shape = shape,
      elevation = ZeroElevation,
      color =
          MaterialTheme.colors.primary.copy(
              alpha = 0.20F,
          ),
      contentColor = MaterialTheme.colors.onPrimary,
  ) {
    Column(
        modifier = Modifier.clickable { onClick() }.padding(MaterialTheme.keylines.content),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Icon(
          modifier =
              Modifier.size(ImageDefaults.LargeSize)
                  .padding(bottom = MaterialTheme.keylines.content),
          imageVector = icon,
          contentDescription = title,
          tint = MaterialTheme.colors.onPrimary,
      )
      Text(
          text = title,
          style =
              MaterialTheme.typography.body1.copy(
                  color =
                      MaterialTheme.colors.onPrimary.copy(
                          alpha = ContentAlpha.medium,
                      ),
              ),
      )
    }
  }
}
