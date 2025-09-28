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

package com.pyamsoft.sleepforbreakfast.spending.automatic.ignore

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.collections.AndroidCollection
import com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.collections.AndroidSystemUiCollection
import com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.collections.FairEmailCollection
import com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.collections.NeoStoreCollection
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AutomaticIgnoresImpl @Inject internal constructor() : AutomaticIgnores {

  override suspend fun shouldIgnoreNotification(
      packageName: String,
      title: CharSequence,
      bigTitle: CharSequence,
      text: CharSequence,
      bigText: CharSequence,
  ): Boolean {
    return ignores(
        packageName = packageName,
        title = title,
        bigTitle = bigTitle,
        text = text,
        bigText = bigText,
        ignoreables =
            setOf(
                AndroidCollection,
                AndroidSystemUiCollection,
                FairEmailCollection,
                NeoStoreCollection,
            ),
    )
  }

  companion object {

    @CheckResult
    private fun shouldIgnore(
        packageName: String,
        title: CharSequence,
        bigTitle: CharSequence,
        text: CharSequence,
        bigText: CharSequence,
        ignorable: Ignorable,
    ): Boolean {
      if (ignorable.packageName != packageName) {
        return false
      }

      ignorable.title?.let { ignore ->
        if (text.isNotBlank()) {
          if (ignore.matches(title)) {
            return true
          }
        }

        if (bigText.isNotBlank()) {
          if (ignore.matches(bigTitle)) {
            return true
          }
        }
      }

      ignorable.text?.let { ignore ->
        if (text.isNotBlank()) {
          if (ignore.matches(text)) {
            return true
          }
        }

        if (bigText.isNotBlank()) {
          if (ignore.matches(bigText)) {
            return true
          }
        }
      }

      return false
    }

    @CheckResult
    private suspend fun ignores(
        packageName: String,
        title: CharSequence,
        bigTitle: CharSequence,
        text: CharSequence,
        bigText: CharSequence,
        ignoreables: Collection<IIgnorable>,
    ): Boolean {
      for (ignore in ignoreables) {
        when (ignore) {
          is Ignorable -> {
            if (
                shouldIgnore(
                    packageName = packageName,
                    title = title,
                    bigTitle = bigTitle,
                    text = text,
                    bigText = bigText,
                    ignorable = ignore,
                )
            ) {
              return true
            }
          }
          is IgnoreCollection -> {
            for (innerIgnore in ignore.ignorables()) {
              if (
                  shouldIgnore(
                      packageName = packageName,
                      title = title,
                      bigTitle = bigTitle,
                      text = text,
                      bigText = bigText,
                      ignorable = innerIgnore,
                  )
              ) {
                return true
              }
            }
          }
        }
      }

      return false
    }
  }
}
