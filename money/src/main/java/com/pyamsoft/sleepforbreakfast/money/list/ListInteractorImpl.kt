package com.pyamsoft.sleepforbreakfast.money.list

import androidx.annotation.CheckResult
import com.pyamsoft.pydroid.core.Enforcer
import com.pyamsoft.pydroid.core.ResultWrapper
import com.pyamsoft.pydroid.util.ifNotCancellation
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

abstract class ListInteractorImpl<I : Any, T : Any, CE : Any> protected constructor() :
    ListInteractor<I, T, CE> {

  final override suspend fun loadAll(force: Boolean): ResultWrapper<List<T>> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        if (force) {
          performClearCache()
        }

        return@withContext try {
          ResultWrapper.success(performQueryAll())
        } catch (e: Throwable) {
          Timber.e(e, "Error loading items")
          ResultWrapper.failure(e)
        }
      }

  final override suspend fun listenForItemChanges(onEvent: (CE) -> Unit) =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext performListenRealtime(onEvent)
      }

  final override suspend fun loadOne(id: I): ResultWrapper<T> =
      withContext(context = Dispatchers.IO) {
        Enforcer.assertOffMainThread()

        return@withContext try {
          when (val res = performQueryOne(id)) {
            is Maybe.Data -> {
              ResultWrapper.success(res.data)
            }
            is Maybe.None -> {
              val err = RuntimeException("Could not find item with ID $id")
              Timber.w(err.message)
              ResultWrapper.failure(err)
            }
          }
        } catch (e: Throwable) {
          Timber.e(e, "Error loading item $id")
          ResultWrapper.failure(e)
        }
      }

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

  @CheckResult protected abstract suspend fun performDelete(item: T): Boolean

  @CheckResult protected abstract suspend fun performQueryAll(): List<T>

  @CheckResult protected abstract suspend fun performQueryOne(id: I): Maybe<out T>

  @CheckResult protected abstract suspend fun performClearCache()

  @CheckResult protected abstract suspend fun performListenRealtime(onEvent: (CE) -> Unit)
}
