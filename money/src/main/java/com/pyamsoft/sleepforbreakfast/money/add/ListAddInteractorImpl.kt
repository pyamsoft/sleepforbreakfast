package com.pyamsoft.sleepforbreakfast.money.add

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class ListAddInteractorImpl<T : Any> protected constructor() : ListAddInteractor<T> {

  final override suspend fun submit(item: T): ResultWrapper<DbInsert.InsertResult<T>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          ResultWrapper.success(performInsert(item))
        } catch (e: Throwable) {
          e.ifNotCancellation {
            Timber.e(e, "Error submitting item: $item")
            ResultWrapper.failure(e)
          }
        }
      }

  @CheckResult protected abstract suspend fun performInsert(item: T): DbInsert.InsertResult<T>
}
