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

package com.pyamsoft.sleepforbreakfast.ui

import android.text.format.DateFormat
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun DatePickerDialog(
    modifier: Modifier = Modifier,
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
  val handleDateSelected by rememberUpdatedState(onDateSelected)

  SurfaceDialog(
      modifier = modifier,
      onDismiss = onDismiss,
  ) {
    AndroidView(
        factory = { ctx ->
          DatePicker(ctx).apply {
            init(
                initialDate.year,
                // Month is 1indexed, but we expect 0
                initialDate.monthValue - 1,
                initialDate.dayOfMonth,
            ) { _, year, month, dayOfMonth ->
              // month is 0 index but we need it to be 1 indexed
              val date = LocalDate.of(year, month + 1, dayOfMonth)
              handleDateSelected(date)
            }
          }
        },
    )
  }
}

@Composable
fun TimePickerDialog(
    modifier: Modifier = Modifier,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit,
    onDismiss: () -> Unit,
) {
  val context = LocalContext.current
  val is24Hours = remember(context) { DateFormat.is24HourFormat(context) }

  val handleTimeSelected by rememberUpdatedState(onTimeSelected)

  SurfaceDialog(
      modifier = modifier,
      onDismiss = onDismiss,
  ) {
    AndroidView(
        factory = { ctx ->
          TimePicker(ctx).apply {
            setIs24HourView(is24Hours)
            minute = initialTime.minute
            hour = initialTime.hour

            setOnTimeChangedListener { _, hour, minute ->
              val time = LocalTime.of(hour, minute)
              handleTimeSelected(time)
            }
          }
        },
    )
  }
}
