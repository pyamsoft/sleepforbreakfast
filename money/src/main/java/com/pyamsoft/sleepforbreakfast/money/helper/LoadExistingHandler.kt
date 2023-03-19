package com.pyamsoft.sleepforbreakfast.money.helper

import kotlinx.coroutines.CoroutineScope

interface LoadExistingHandler<P : Any, R : Any> {

  fun loadExisting(
      scope: CoroutineScope,
      id: P,
      onLoaded: (R) -> Unit,
  )
}
