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

package com.pyamsoft.sleepforbreakfast.worker.work.automatictransaction

import com.pyamsoft.sleepforbreakfast.core.Timber
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.worker.work.BgWorker
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AutomaticSpendingWork
@Inject
internal constructor(
    private val automaticQueryDao: AutomaticQueryDao,
    private val handler: AutomaticTransactionHandler,
) : BgWorker {

  private suspend fun processJobs() =
      GLOBAL_LOCK.withLock {
        val unconsumed = automaticQueryDao.queryUnused()

        for (auto in unconsumed) {
          // Maybe I suck at SQL
          if (auto.used) {
            continue
          }

          handler.process(auto)
        }
      }

  override suspend fun work(): BgWorker.WorkResult =
      withContext(context = Dispatchers.Default) {
        try {
          processJobs()
          return@withContext BgWorker.WorkResult.Success
        } catch (e: Throwable) {
          if (e is CancellationException) {
            Timber.w { "Job cancelled during processing" }
            return@withContext BgWorker.WorkResult.Cancelled
          } else {
            Timber.e(e) { "Error during processing of unconsumed automatics" }
            return@withContext BgWorker.WorkResult.Failed(e)
          }
        }
      }

  companion object {

    /**
     * the global lock prevents multiple callers from running this handler at the same time as it
     * could cause duplicates in the DB if operations are close enough
     */
    private val GLOBAL_LOCK = Mutex()
  }
}
