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

package com.pyamsoft.sleepforbreakfast.category.delete

import androidx.annotation.CheckResult
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent

@Subcomponent(
    modules =
        [
            CategoryDeleteComponent.CategoryModule::class,
        ],
)
internal interface CategoryDeleteComponent {

  fun inject(injector: CategoryDeleteInjector)

  @Subcomponent.Factory
  interface Factory {

    @CheckResult
    fun create(
        @BindsInstance params: CategoryDeleteParams,
    ): CategoryDeleteComponent
  }

  @Module abstract class CategoryModule
}
