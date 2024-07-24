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

package com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.collections

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.Ignorable
import com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.IgnoreCollection

internal abstract class AbstractIgnoreCollection(
    private val packageName: String,
) : IgnoreCollection {

  @CheckResult
  protected fun ignoreText(regex: Regex): Ignorable {
    return Ignorable(
        packageName = packageName,
        text = regex,
    )
  }

  @CheckResult
  protected fun ignoreText(text: String): Ignorable {
    return ignoreText(
        regex = Regex(text),
    )
  }

  @CheckResult
  protected fun ignoreTitle(regex: Regex): Ignorable {
    return Ignorable(
        packageName = packageName,
        title = regex,
    )
  }

  @CheckResult
  protected fun ignoreTitle(text: String): Ignorable {
    return ignoreTitle(
        regex = Regex(text),
    )
  }
}
