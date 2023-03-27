package com.pyamsoft.sleepforbreakfast.db

import androidx.annotation.CheckResult

interface ActivateModel<T : Any> {

  @get:CheckResult val active: Boolean

  @get:CheckResult val archived: Boolean

  @CheckResult fun activate(): T

  @CheckResult fun deactivate(): T

  @CheckResult fun archive(): T

  @CheckResult fun unarchive(): T
}
