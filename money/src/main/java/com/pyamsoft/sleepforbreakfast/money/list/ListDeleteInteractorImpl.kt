package com.pyamsoft.sleepforbreakfast.money.list

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class ListDeleteInteractorImpl<T : Any> protected constructor() : ListDeleteInteractor<T> {

  final override suspend fun delete(item: T): ResultWrapper<Boolean> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          ResultWrapper.success(performDelete(item))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error deleting item: $item")
            ResultWrapper.failure(e)
          }
        }
      }

  @CheckResult protected abstract suspend fun performDelete(item: T): Boolean
}
