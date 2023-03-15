/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.sleepforbreakfast.transaction.delete

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.transactions.delete.TransactionDeleteParams
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent

@Subcomponent(
    modules =
        [
            TransactionDeleteComponent.TransactionModule::class,
        ],
)
internal interface TransactionDeleteComponent {

  fun inject(injector: TransactionDeleteInjector)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance params: TransactionDeleteParams,
    ): TransactionDeleteComponent
  }

  @Module abstract class TransactionModule
}
