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
