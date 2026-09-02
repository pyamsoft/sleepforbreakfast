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

package com.pyamsoft.sleepforbreakfast.spending.automatic

import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.core.app.NotificationCompat
import com.pyamsoft.pydroid.core.LintIgnoreLongMethod
import com.pyamsoft.pydroid.core.LintIgnoreTooGenericExceptionCaught
import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.spending.AutomaticHandler
import com.pyamsoft.sleepforbreakfast.spending.PaymentNotification
import com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.AutomaticIgnores

internal abstract class BaseAutomaticHandler
protected constructor(
    private val ignores: AutomaticIgnores,
) : AutomaticHandler {

  @CheckResult
  private fun MatchGroupCollection.extractGroup(group: String): String? =
      try {
        this.get(name = group)?.value?.trim()
      } catch (_: Throwable) {
        // This throws if it can't find the group, oh well
        null
      }

  @CheckResult
  private fun getTitle(
      title: CharSequence,
      bigTitle: CharSequence,
  ): CharSequence {
    return if (title.isNotBlank()) {
      title
    } else if (bigTitle.isNotBlank()) {
      bigTitle
    } else {
      DEFAULT_TITLE
    }
  }

  @CheckResult
  @LintIgnoreLongMethod
  private suspend fun handleRegex(
      notificationId: Int,
      packageName: String,
      regexMatch: RegexMatch,
      payText: CharSequence,
      title: CharSequence,
      bigTitle: CharSequence,
  ): PaymentNotification? {
    val regex = regexMatch.regex
    if (!regex.containsMatchIn(payText)) {
      Timber.w {
        "${handlerId()}: Could not match notification ${
                  mapOf(
                      "notificationId" to notificationId,
                      "package" to packageName,
                      "payText" to payText,
                      "title" to title,
                      "bigTitle" to bigTitle,
                      "regex" to regex.pattern,
                  )
              }"
      }
      return null
    }

    val capture = regex.find(payText)
    if (capture == null) {
      Timber.w { "${handlerId()}: Unable to capture from payText: $payText" }
      return null
    }

    val captureGroups = capture.groups

    val justPrice =
        captureGroups
            .extractGroup(CAPTURE_NAME_AMOUNT)
            ?.replace(REGEX_FILTER_ONLY_DIGITS, "")
            ?.toLongOrNull()

    if (justPrice == null) {
      Timber.w {
        "${handlerId()}: Unable to get justPrice from payText: $payText ${
                mapOf(
                    "notificationId" to notificationId,
                    "package" to packageName,
                    "payText" to payText,
                    "title" to title,
                    "bigTitle" to bigTitle,
                    "regex" to regex.pattern,
                )
            }"
      }
      return null
    }

    val name = getTitle(title, bigTitle)
    val optionalAccount = captureGroups.extractGroup(CAPTURE_NAME_ACCOUNT).orEmpty()
    val optionalDate = captureGroups.extractGroup(CAPTURE_NAME_DATE).orEmpty()
    val optionalMerchant = captureGroups.extractGroup(CAPTURE_NAME_MERCHANT).orEmpty()
    val optionalDescription = captureGroups.extractGroup(CAPTURE_NAME_DESCRIPTION).orEmpty()

    return PaymentNotification(
        regexMatch = regexMatch,
        title = name.toString(),
        text = capture.value,
        type = getType(),
        categories = getCategories(),
        amount = justPrice,
        optionalAccount = optionalAccount,
        optionalDate = optionalDate,
        optionalDescription = optionalDescription,
        optionalMerchant = optionalMerchant,
    )
  }

  @CheckResult
  @LintIgnoreLongMethod
  private suspend fun extractFromCandidates(
      notificationId: Int,
      packageName: String,
      regexList: Collection<RegexMatch>,
      candidates: List<CharSequence>,
      title: CharSequence,
      bigTitle: CharSequence,
  ): PaymentNotification? {
    for (regex in regexList) {
      for (candidate in candidates) {
        if (candidate.isBlank()) {
          continue
        }

        // If regex is bad, we catch and result NULL
        val result =
            try {
              handleRegex(
                  notificationId = notificationId,
                  packageName = packageName,
                  regexMatch = regex,
                  payText = candidate,
                  title = title,
                  bigTitle = bigTitle,
              )
            } catch (@LintIgnoreTooGenericExceptionCaught e: Throwable) {
              Timber.e(e) {
                "${handlerId()}: Failed to compile regex ${
                            mapOf(
                                "notificationId" to notificationId,
                                "package" to packageName,
                                "payText" to candidate,
                                "title" to title,
                                "bigTitle" to bigTitle,
                                "regex" to regex.regex.pattern,
                            )
                        }"
              }
              null
            }

        if (result != null) {
          Timber.d {
            "${handlerId()}: Notification handled! ${
                        mapOf(
                            "notificationId" to notificationId,
                            "payText" to candidate,
                            "title" to title,
                            "bigTitle" to bigTitle,
                            "result" to result,
                        )
                    }"
          }
          return result
        }
      }
    }

    return null
  }

  @CheckResult
  private suspend fun extractGroupedNotification(
      notificationId: Int,
      packageName: String,
      title: CharSequence,
      bigTitle: CharSequence,
      regexList: Collection<RegexMatch>,
      lines: List<CharSequence>,
  ): List<PaymentNotification> {
    val results =
        lines
            .filter { line ->
              !ignores.shouldIgnoreNotification(
                  packageName = packageName,
                  title = title,
                  bigTitle = bigTitle,
                  text = line,
                  bigText = "",
              )
            }
            .mapNotNull { line ->
              extractFromCandidates(
                  notificationId = notificationId,
                  packageName = packageName,
                  regexList = regexList,
                  candidates = listOf(line),
                  title = title,
                  bigTitle = bigTitle,
              )
            }

    if (results.isEmpty()) {
      Timber.w {
        "${handlerId()}: No regexes handled grouped notification ${
                    mapOf(
                        "notificationId" to notificationId,
                        "title" to title,
                        "bigTitle" to bigTitle,
                        "lines" to lines,
                    )
                }"
      }
    }

    return results
  }

  @CheckResult
  private suspend fun extractSingleNotification(
      notificationId: Int,
      packageName: String,
      title: CharSequence,
      bigTitle: CharSequence,
      regexList: Collection<RegexMatch>,
      bundle: Bundle,
  ): List<PaymentNotification> {
    val text = bundle.getCharSequence(NotificationCompat.EXTRA_TEXT, "")
    val bigText = bundle.getCharSequence(NotificationCompat.EXTRA_BIG_TEXT, "")
    if (
        ignores.shouldIgnoreNotification(
            packageName = packageName,
            title = title,
            bigTitle = bigTitle,
            text = text,
            bigText = bigText,
        )
    ) {
      return emptyList()
    }

    val result =
        extractFromCandidates(
            notificationId = notificationId,
            packageName = packageName,
            regexList = regexList,
            candidates = listOf(text, bigText),
            title = title,
            bigTitle = bigTitle,
        )

    if (result == null) {
      Timber.w {
        "${handlerId()}: No regexes handled single notification ${
                    mapOf(
                        "notificationId" to notificationId,
                        "text" to text,
                        "bigText" to bigText,
                        "title" to title,
                        "bigTitle" to bigTitle,
                    )
                }"
      }
    }

    return listOfNotNull(result)
  }

  final override suspend fun extract(
      notificationId: Int,
      packageName: String,
      bundle: Bundle,
  ): List<PaymentNotification> {
    val title = bundle.getCharSequence(NotificationCompat.EXTRA_TITLE, "")
    val bigTitle = bundle.getCharSequence(NotificationCompat.EXTRA_TITLE_BIG, "")

    val regexList = getPossibleRegexes()

    // If the notification collpases, the individual notification data will be in this extra
    val lines = bundle.getCharSequenceArray(NotificationCompat.EXTRA_TEXT_LINES)?.toList().orEmpty()
    if (lines.isNotEmpty()) {
      Timber.d { "${handlerId()}: Extracting grouped notification" }
      return extractGroupedNotification(
          notificationId = notificationId,
          packageName = packageName,
          title = title,
          bigTitle = bigTitle,
          regexList = regexList,
          lines = lines,
      )
    }

    // Otherwise its a single notification, so read it's text bits
    Timber.d { "${handlerId()}: Extracting single notification" }
    return extractSingleNotification(
        notificationId = notificationId,
        packageName = packageName,
        title = title,
        bigTitle = bigTitle,
        regexList = regexList,
        bundle = bundle,
    )
  }

  @CheckResult protected open suspend fun getCategories(): Set<DbCategory.Id> = emptySet()

  @CheckResult protected abstract fun getPossibleRegexes(): Collection<RegexMatch>

  @CheckResult protected abstract fun getType(): DbTransaction.Type

  @CheckResult protected abstract fun handlerId(): String

  data class RegexMatch(
      val id: String,
      val regex: Regex,
  )

  companion object {
    private const val DEFAULT_TITLE = "Automatic Transaction"
  }
}
