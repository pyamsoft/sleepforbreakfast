/*
 * Copyright 2026 pyamsoft
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

package com.pyamsoft.sleepforbreakfast.automatic.add

import androidx.annotation.CheckResult
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.pyamsoft.pydroid.theme.keylines
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.ui.icons.IconPainters
import java.util.regex.PatternSyntaxException

private val REGEX_BORDER_WIDTH: Dp = 2.dp
private val REGEX_ICON_SIZE: Dp = 24.dp

private enum class RegexMatch {
  NONE,
  PASS,
  FAIL,
}

@CheckResult
private fun matchRegex(
    pattern: String,
    testText: String,
): RegexMatch {
  if (testText.isBlank() || pattern.isBlank()) {
    return RegexMatch.NONE
  }

  return try {
    val matches = Regex(pattern, RegexOption.MULTILINE).containsMatchIn(testText)
    if (matches) RegexMatch.PASS else RegexMatch.FAIL
  } catch (e: PatternSyntaxException) {
    Timber.e(e) { "Invalid regex pattern: $pattern" }
    RegexMatch.FAIL
  }
}

@Composable
internal fun AutomaticAddRegexTestField(
    modifier: Modifier = Modifier,
    testText: String,
    onTestTextChanged: (String) -> Unit,
) {
  Column(
      modifier = modifier,
  ) {
    Text(
        modifier = Modifier.padding(bottom = MaterialTheme.keylines.baseline),
        text = "Test Text",
        fontWeight = FontWeight.W700,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodySmall,
    )
    TextField(
        modifier = Modifier.fillMaxWidth(),
        value = testText,
        onValueChange = onTestTextChanged,
        label = {
          Text(
              text = "Sample Notification Text",
          )
        },
        placeholder = {
          Text(
              text = "Type text here to test it against your regexes below",
          )
        },
    )
  }
}

@Composable
internal fun AutomaticAddRegexRow(
    modifier: Modifier = Modifier,
    regex: AutomaticAddViewState.BuildMatchRegex,
    testText: String,
    onRegexChanged: (AutomaticAddViewState.BuildMatchRegex) -> Unit,
) {
  val matches = remember(regex.text, testText) { matchRegex(regex.text, testText) }

  val passColor = MaterialTheme.colorScheme.primary
  val failColor = MaterialTheme.colorScheme.error
  val neutralColor = MaterialTheme.colorScheme.outline
  val statusColor =
      remember(
          matches,
          passColor,
          failColor,
          neutralColor,
      ) {
        when (matches) {
          RegexMatch.NONE -> neutralColor
          RegexMatch.PASS -> passColor
          RegexMatch.FAIL -> failColor
        }
      }

  Row(
      modifier = modifier.fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
  ) {
    TextField(
        modifier =
            Modifier.weight(1F)
                .border(
                    width = REGEX_BORDER_WIDTH,
                    color = statusColor,
                    shape = MaterialTheme.shapes.small,
                ),
        value = regex.text,
        onValueChange = { v ->
          val changed = regex.copy(text = v)
          onRegexChanged(changed)
        },
        label = {
          Text(
              // TODO language res file
              text = "Regex",
          )
        },
        placeholder = {
          Text(
              // TODO language res file
              text = "Your test regular expression",
          )
        },
    )

    when (matches) {
      RegexMatch.NONE -> {
        Spacer(
            modifier =
                Modifier.padding(start = MaterialTheme.keylines.baseline).size(REGEX_ICON_SIZE),
        )
      }
      RegexMatch.PASS,
      RegexMatch.FAIL -> {
        val painter: Painter
        val description: String
        if (matches == RegexMatch.PASS) {
          painter = IconPainters.check()
          description = "Matches"
        } else {
          painter = IconPainters.cancel()
          description = "No Match"
        }
        Icon(
            modifier =
                Modifier.padding(start = MaterialTheme.keylines.baseline).size(REGEX_ICON_SIZE),
            painter = painter,
            contentDescription = description,
            tint = statusColor,
        )
      }
    }
  }
}
