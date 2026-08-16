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

package com.pyamsoft.sleepforbreakfast.transaction

import androidx.activity.ComponentActivity
import com.pyamsoft.pydroid.ui.inject.ComposableInjector
import com.pyamsoft.sleepforbreakfast.ObjectGraph
import com.pyamsoft.sleepforbreakfast.main.MainPage
import com.pyamsoft.sleepforbreakfast.transactions.TransactionViewModeler
import java.time.Clock
import javax.inject.Inject

internal class TransactionInjector
@Inject
internal constructor(
    private val page: MainPage.Transactions,
) : ComposableInjector() {

  @JvmField @Inject internal var viewModel: TransactionViewModeler? = null
  @JvmField @Inject internal var clock: Clock? = null

  override fun onInject(activity: ComponentActivity) {
    ObjectGraph.ActivityScope.retrieve(activity)
        .plusTransactions()
        .create(
            dateRange = page.range,
            categoryId = page.categoryId,
            showAllTransactions = page.showAllTransactions,
        )
        .inject(this)
  }

  override fun onDispose() {
    viewModel = null
    clock = null
  }
}
