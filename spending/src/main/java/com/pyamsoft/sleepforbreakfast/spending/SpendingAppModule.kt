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

package com.pyamsoft.sleepforbreakfast.spending

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.spending.automatic.AutomaticManager
import com.pyamsoft.sleepforbreakfast.spending.automatic.AutomaticManagerImpl
import com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.AutomaticIgnores
import com.pyamsoft.sleepforbreakfast.spending.automatic.ignore.AutomaticIgnoresImpl
import dagger.Binds
import dagger.Module
import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) internal annotation class InternalApi

@Module
abstract class SpendingAppModule {

  @Binds
  @CheckResult
  internal abstract fun bindAutomaticManager(impl: AutomaticManagerImpl): AutomaticManager

  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindAutomaticIgnores(impl: AutomaticIgnoresImpl): AutomaticIgnores

  @Binds
  @CheckResult
  internal abstract fun bindSpendingHandler(
      impl: SpendingTrackerHandlerImpl
  ): SpendingTrackerHandler

  @Binds @CheckResult internal abstract fun bindTester(impl: DefaultSpendingTester): SpendingTester
}
