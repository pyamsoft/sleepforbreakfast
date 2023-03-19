package com.pyamsoft.sleepforbreakfast.money.helper

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.ResultWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class LoadExistingHandlerImpl<P : Any, R : Any> protected constructor() :
    LoadExistingHandler<P, R> {

  final override fun loadExisting(
      scope: CoroutineScope,
      id: P,
      onLoaded: (R) -> Unit,
  ) {
    if (!isIdEmpty(id)) {
      scope.launch(context = Dispatchers.Main) {
        loadData(id)
            .onSuccess { result ->
              Timber.d("Loaded data for editing: $result")
              onLoaded(result)
            }
            .onFailure { Timber.e(it, "Error loading data for editing: $id") }
      }
    }
  }

  @CheckResult protected abstract suspend fun loadData(id: P): ResultWrapper<R>

  @CheckResult protected abstract fun isIdEmpty(id: P): Boolean
}
