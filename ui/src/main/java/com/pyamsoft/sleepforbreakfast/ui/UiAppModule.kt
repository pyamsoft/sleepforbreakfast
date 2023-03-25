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

package com.pyamsoft.sleepforbreakfast.ui

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.ui.savedstate.JsonParser
import com.pyamsoft.sleepforbreakfast.ui.savedstate.MoshiJsonParser
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier @Retention(AnnotationRetention.BINARY) internal annotation class InternalApi

@Module
abstract class UiAppModule {

  @Binds @CheckResult internal abstract fun bindMoshiJsonParser(impl: MoshiJsonParser): JsonParser

  @Module
  companion object {

    @Provides
    @JvmStatic
    @Singleton
    @InternalApi
    internal fun provideMoshi(): Moshi {
      return Moshi.Builder().build()
    }
  }
}
