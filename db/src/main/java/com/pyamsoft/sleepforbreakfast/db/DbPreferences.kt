package com.pyamsoft.sleepforbreakfast.db

import androidx.annotation.CheckResult
import kotlinx.coroutines.flow.Flow

interface DbPreferences {

  @CheckResult fun listenSystemCategoriesPreloaded(): Flow<Boolean>

  fun markSystemCategoriesPreloaded()
}
