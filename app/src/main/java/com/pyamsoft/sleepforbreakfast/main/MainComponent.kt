/*
 * Copyright 2023 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.main

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.category.CategoryComponent
import com.pyamsoft.sleepforbreakfast.category.add.CategoryAddComponent
import com.pyamsoft.sleepforbreakfast.category.delete.CategoryDeleteComponent
import com.pyamsoft.sleepforbreakfast.core.ActivityScope
import com.pyamsoft.sleepforbreakfast.home.HomeComponent
import com.pyamsoft.sleepforbreakfast.transaction.TransactionComponent
import com.pyamsoft.sleepforbreakfast.transaction.add.TransactionAddComponent
import com.pyamsoft.sleepforbreakfast.transaction.delete.TransactionDeleteComponent
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent

@ActivityScope
@Subcomponent(
    modules =
        [
            MainComponent.MainModule::class,
        ],
)
internal interface MainComponent {

  fun inject(activity: MainActivity)

  fun inject(injector: MainInjector)

  // Home
  @CheckResult fun plusHome(): HomeComponent.Factory

  // Category
  @CheckResult fun plusCategory(): CategoryComponent.Factory

  @CheckResult fun plusAddCategory(): CategoryAddComponent.Factory

  @CheckResult fun plusDeleteCategory(): CategoryDeleteComponent.Factory

  // Transactions
  @CheckResult fun plusTransactions(): TransactionComponent.Factory

  @CheckResult fun plusAddTransactions(): TransactionAddComponent.Factory

  @CheckResult fun plusDeleteTransactions(): TransactionDeleteComponent.Factory

  @Subcomponent.Factory
  interface Factory {

    @CheckResult fun create(@BindsInstance activity: MainActivity): MainComponent
  }

  @Module abstract class MainModule
}
