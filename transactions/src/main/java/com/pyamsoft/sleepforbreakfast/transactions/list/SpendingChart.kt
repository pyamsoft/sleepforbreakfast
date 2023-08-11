package com.pyamsoft.sleepforbreakfast.transactions.list

import androidx.annotation.CheckResult
import androidx.compose.foundation.layout.Box
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.patrykandpatrick.vico.compose.axis.horizontal.bottomAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.chart.column.ColumnChart
import com.patrykandpatrick.vico.core.component.shape.LineComponent
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModel
import com.patrykandpatrick.vico.core.entry.FloatEntry
import com.patrykandpatrick.vico.core.entry.composed.plus
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.pyamsoft.pydroid.ui.util.collectAsStateList
import com.pyamsoft.pydroid.ui.util.rememberNotNull
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.money.list.KnobBar
import com.pyamsoft.sleepforbreakfast.money.list.UsageIndicator
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewState
import com.pyamsoft.sleepforbreakfast.ui.COLOR_EARN
import com.pyamsoft.sleepforbreakfast.ui.COLOR_SPEND
import com.pyamsoft.sleepforbreakfast.ui.icons.BarChart
import java.time.Clock
import java.time.LocalDate
import kotlin.math.roundToInt
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
internal fun SpendingChart(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    onToggle: () -> Unit,
) {
  val isOpen by state.isChartOpen.collectAsState()

  Box(
      modifier = modifier,
      contentAlignment = Alignment.BottomEnd,
  ) {
    IconButton(
        onClick = onToggle,
    ) {
      Icon(
          imageVector = Icons.Filled.BarChart,
          contentDescription = "Chart",
          tint =
              MaterialTheme.colors.onPrimary.copy(
                  alpha = if (isOpen) ContentAlpha.high else ContentAlpha.medium,
              ),
      )
    }

    UsageIndicator(
        show = isOpen,
    )
  }
}

private data class ChartData(
    val models: ChartEntryModel,
    val columns: List<LineComponent>,
)

@CheckResult
private suspend fun generateChartEntries(
    clock: Clock,
    transactions: SnapshotStateList<DbTransaction>
): List<List<FloatEntry>> =
    withContext(context = Dispatchers.Default) {
      val now = LocalDate.now(clock)

      val entriesWithData =
          transactions
              .asSequence()
              // Entries in this month
              .filter { it.date.month == now.month }
              // 1 bar per day
              .groupBy { it.date.dayOfMonth }
              // Sum up the spending values
              .mapValues { entry ->
                entry.value.sumOf {
                  it.amountInCents *
                      when (it.type) {
                        DbTransaction.Type.SPEND -> -1
                        DbTransaction.Type.EARN -> 1
                      }
                }
              }

      val totalDays = now.month.length(now.isLeapYear)
      return@withContext mutableListOf<List<FloatEntry>>().apply {
        for (date in 1..totalDays) {
          val data = entriesWithData[date]

          // Chart needs our X to start at 0
          val x = date.minus(1).toFloat()
          if (data == null) {
            add(
                listOf(
                    FloatEntry(
                        x = x,
                        y = 0F,
                    ),
                ),
            )
          } else {
            add(
                listOf(
                    FloatEntry(
                        x = x,
                        y = data.toFloat().div(100),
                    ),
                ),
            )
          }
        }
      }
    }

@CheckResult
private fun generateLineColors(entries: List<List<ChartEntry>>): List<LineComponent> {
  return entries
      .asSequence()
      .flatMap { it }
      .map { if (it.y == 0F) Color.Unspecified else if (it.y > 0) COLOR_EARN else COLOR_SPEND }
      .map { color ->
        LineComponent(
            color = color.toArgb(),
            thicknessDp = 16F,
        )
      }
      .toList()
}

@CheckResult
@Composable
private fun rememberChart(
    clock: Clock,
    transactions: SnapshotStateList<DbTransaction>
): ChartData? {
  val (data, setData) = remember { mutableStateOf<ChartData?>(null) }

  LaunchedEffect(transactions) {
    val scope = this
    scope.launch(context = Dispatchers.Default) {
      val entries = generateChartEntries(clock, transactions)

      // Create a composite chart (we do this because each LineColor is applied to a different chart
      // entry model)
      // So instead of one model with X number of floatEntry values, we have X models with 1 float
      // entry value for each
      var models: ChartEntryModel? = null
      entries.forEach { e ->
        models =
            models.let { m ->
              if (m == null) {
                entryModelOf(e)
              } else {
                m + entryModelOf(e)
              }
            }
      }

      models.let { m ->
        if (m == null) {
          setData(null)
        } else {
          setData(
              ChartData(
                  models = m,
                  columns = generateLineColors(entries),
              ),
          )
        }
      }
    }
  }

  return data
}

@Composable
internal fun SpendingChartBar(
    modifier: Modifier = Modifier,
    state: TransactionViewState,
    clock: Clock,
    onToggle: () -> Unit,
) {
  val isOpen by state.isChartOpen.collectAsState()
  val transactions = state.items.collectAsStateList()

  val chart = rememberChart(clock, transactions)

  KnobBar(
      modifier = modifier,
      isOpen = isOpen && chart != null,
      onToggle = onToggle,
  ) {
    val c = rememberNotNull(chart)

    Chart(
        modifier = Modifier.weight(1F),
        model = c.models,
        chart =
            columnChart(
                columns = c.columns,
                mergeMode = ColumnChart.MergeMode.Stack,
            ),
        bottomAxis =
            bottomAxis(
                // The first day is "0" change it to -> "1"
                valueFormatter = { v, _ -> "${v.plus(1).roundToInt()}" },
            ),
    )
  }
}
