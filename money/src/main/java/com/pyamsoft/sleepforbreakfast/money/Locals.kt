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

package com.pyamsoft.sleepforbreakfast.money

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.pyamsoft.sleepforbreakfast.money.observer.CategoryObserver
import com.pyamsoft.sleepforbreakfast.money.observer.TransactionObserver

@JvmField
val LocalCategoryContainerColor: ProvidableCompositionLocal<Color> = staticCompositionLocalOf {
  Color.Unspecified
}

@JvmField
val LocalCategoryContentColor: ProvidableCompositionLocal<Color> = staticCompositionLocalOf {
  Color.Unspecified
}

@JvmField
val LocalCategoryObserver: ProvidableCompositionLocal<CategoryObserver> = staticCompositionLocalOf {
  throw IllegalStateException("LocalCategoryObserver must be provided")
}

@JvmField
val LocalTransactionObserver: ProvidableCompositionLocal<TransactionObserver> =
    staticCompositionLocalOf {
      throw IllegalStateException("LocalTransactionObserver must be provided")
    }
