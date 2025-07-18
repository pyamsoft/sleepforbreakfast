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

package com.pyamsoft.sleepforbreakfast.money

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoader
import com.pyamsoft.sleepforbreakfast.money.category.CategoryLoaderImpl
import com.pyamsoft.sleepforbreakfast.money.observer.CategoryObserver
import com.pyamsoft.sleepforbreakfast.money.observer.DefaultCategoryObserver
import com.pyamsoft.sleepforbreakfast.money.observer.DefaultTransactionObserver
import com.pyamsoft.sleepforbreakfast.money.observer.TransactionObserver
import dagger.Binds
import dagger.Module

@Module
abstract class MoneyAppModule {

  @Binds
  internal abstract fun provideTransactionObserver(
      impl: DefaultTransactionObserver
  ): TransactionObserver

  @Binds
  internal abstract fun provideCategoryObserver(impl: DefaultCategoryObserver): CategoryObserver

  @Binds
  @CheckResult
  internal abstract fun bindCategoryLoader(impl: CategoryLoaderImpl): CategoryLoader
}
