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

package com.pyamsoft.sleepforbreakfast.transactions

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.transactions.auto.TransactionAutoInteractor
import com.pyamsoft.sleepforbreakfast.transactions.auto.TransactionAutoInteractorImpl
import com.pyamsoft.sleepforbreakfast.transactions.repeat.TransactionRepeatInteractor
import com.pyamsoft.sleepforbreakfast.transactions.repeat.TransactionRepeatInteractorImpl
import dagger.Binds
import dagger.Module

@Module
abstract class TransactionAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindInteractor(impl: TransactionInteractorImpl): TransactionInteractor

  @Binds
  @CheckResult
  internal abstract fun bindRepeatInteractor(
      impl: TransactionRepeatInteractorImpl
  ): TransactionRepeatInteractor

  @Binds
  @CheckResult
  internal abstract fun bindAutoInteractor(
      impl: TransactionAutoInteractorImpl
  ): TransactionAutoInteractor
}
