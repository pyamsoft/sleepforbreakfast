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

package com.pyamsoft.sleepforbreakfast.worker.work.repeattransaction

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.replaceCategories
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

@Singleton
internal class RepeatTransactionHandler
@Inject
internal constructor(
    private val repeatQueryDao: RepeatQueryDao,
    private val repeatInsertDao: RepeatInsertDao,
    private val transactionInsertDao: TransactionInsertDao,
    private val clock: Clock,
) {

  @CheckResult
  private fun createTransaction(
      repeat: DbRepeat,
      date: LocalDate,
  ): DbTransaction {
    return DbTransaction.create(clock, DbTransaction.Id.EMPTY)
        // Link to this Repeat instance
        .repeatId(repeat.id)
        .type(repeat.transactionType)
        .name(repeat.transactionName)
        .note(repeat.transactionNote)
        .date(date.atTime(LocalTime.now(clock)))
        .amountInCents(repeat.transactionAmountInCents)
        .replaceCategories(repeat.transactionCategories)
  }

  private suspend fun markRepeatUsed(repeat: DbRepeat, today: LocalDate) {
    when (val res = repeatInsertDao.insert(repeat.lastRunDay(today))) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(
            res.error,
            "Failed updating Repeat to lastUsed: ${mapOf(
                      "repeat" to repeat,
                      "date" to today,
                  )}")
      }
      is DbInsert.InsertResult.Insert -> {
        Timber.d(
            "Inserted new repeat, should this happen?: ${mapOf(
                      "repeat" to repeat,
                      "date" to today,
                  )}")
      }
      is DbInsert.InsertResult.Update -> {
        Timber.d(
            "Updated existing transaction for repeat. Should this happen? ${mapOf(
                      "repeat" to repeat,
                      "transaction" to res.data,
                  )}")
      }
    }
  }

  private suspend fun insertNewTransaction(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    // Make a new transaction and put it into the table
    val transaction =
        createTransaction(
            repeat = repeat,

            // If we have no previous attempt, use the "first day" otherwise use today
            // This is only ran when "filling in", where a repeat is created AFTER the date it
            // is meant to repeat on and we are missing the first one.
            date = if (repeat.lastRunDay == null) repeat.firstDay else today,
        )

    when (val res = transactionInsertDao.insert(transaction)) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(
            res.error,
            "Failed inserting new Transaction made by repeat: ${mapOf(
                    "repeat" to repeat,
                    "transaction" to transaction,
                )}")
      }
      is DbInsert.InsertResult.Insert -> {
        Timber.d(
            "Inserted new transaction for repeat: ${mapOf(
              "repeat" to repeat,
              "transaction" to res.data,
          )}")
      }
      is DbInsert.InsertResult.Update -> {
        Timber.d(
            "Updated existing transaction for repeat. Should this happen? ${mapOf(
                  "repeat" to repeat,
                  "transaction" to res.data,
              )}")
      }
    }
  }

  private suspend fun executeRepeat(repeat: DbRepeat, today: LocalDate) = coroutineScope {
    // Launch these to run them in parallel
    awaitAll<Any>(
        async(context = Dispatchers.Default) { insertNewTransaction(repeat, today) },
        async(context = Dispatchers.Default) { markRepeatUsed(repeat, today) },
    )
  }

  @CheckResult
  private fun createTransactionFromDailyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): Boolean {
    val mostRecentCreated = repeat.lastRunDay

    // If we have no recent transaction, or the recent transaction is before today,
    // we may be able to
    //
    // else
    //
    // The most recent date is the same before today, we may be able to
    var canCreate = false
    if (mostRecentCreated == null) {
      canCreate = true
    } else if (mostRecentCreated < today) {
      val previousWasYesterday = mostRecentCreated.dayOfYear < today.dayOfYear
      if (previousWasYesterday) {
        canCreate = true
      } else {
        Timber.w(
            "Not running repeat, days don't match up ${mapOf(
                "today" to today,
                "firstDay" to repeat.firstDay,
            )}")
      }
    }

    return canCreate
  }

  @CheckResult
  private fun createTransactionFromWeeklyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): Boolean {
    val mostRecentCreated = repeat.lastRunDay

    // If we have no recent transaction, or the recent transaction is before today,
    // we may be able to
    //
    // else
    //
    // The most recent date is the same before today, we may be able to
    var canCreate = false
    if (mostRecentCreated == null) {
      canCreate = true
    } else if (mostRecentCreated < today) {
      val previousWasLastWeek = mostRecentCreated.dayOfMonth < today.dayOfMonth
      val isCurrentDay = today.dayOfWeek == repeat.firstDay.dayOfWeek
      if (previousWasLastWeek && isCurrentDay) {
        canCreate = true
      } else {
        Timber.w(
            "Not running repeat, days don't match up ${mapOf(
                "previousWasLastWeek" to previousWasLastWeek,
                "today" to today,
                "firstDay" to repeat.firstDay,
                "dayOfWeek" to repeat.firstDay.dayOfWeek
            )}")
      }
    }

    return canCreate
  }

  @CheckResult
  private fun createTransactionFromMonthlyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): Boolean {
    val mostRecentCreated = repeat.lastRunDay

    var canCreate = false

    // If we have no recent transaction we can create if the day matches
    if (mostRecentCreated == null) {
      canCreate = true
    } else if (mostRecentCreated < today) {
      val previousWasLastMonth = mostRecentCreated.month < today.month
      val isCurrentDay = today.dayOfMonth == repeat.firstDay.dayOfMonth
      if (previousWasLastMonth && isCurrentDay) {
        canCreate = true
      } else {
        Timber.w(
            "Not running repeat, days don't match up ${mapOf(
                  "previousWasLastMonth" to previousWasLastMonth,
                  "isCurrentDay" to isCurrentDay,
                  "today" to today,
                  "firstDay" to repeat.firstDay,
                  "dayOfMonth" to repeat.firstDay.dayOfMonth
              )}")
      }
    }

    return canCreate
  }

  @CheckResult
  private fun createTransactionFromYearlyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): Boolean {
    var canCreate = false

    val mostRecentCreated = repeat.lastRunDay

    // If we have no recent transaction we can create if the day matches
    if (mostRecentCreated == null) {
      canCreate = true
    } else if (mostRecentCreated < today) {
      // If the most recently created date is before today, maybe

      // The month is last month
      val previousWasLastYear = mostRecentCreated.year < today.year
      val isCurrentDay = today.dayOfYear == repeat.firstDay.dayOfYear
      if (previousWasLastYear && isCurrentDay) {
        canCreate = true
      } else {
        Timber.w(
            "Not running repeat, days don't match up ${mapOf(
              "previousWasLastYear" to previousWasLastYear,
              "isCurrentDay" to isCurrentDay,
              "today" to today,
              "firstDay" to repeat.firstDay,
              "dayOfYear" to repeat.firstDay.dayOfYear
          )}")
      }
    }

    return canCreate
  }

  suspend fun process(
      repeatId: DbRepeat.Id,
      today: LocalDate,
  ) =
      GLOBAL_LOCK.withLock {
        // Query here in case a previous DB operation caused this to be used
        when (val res = repeatQueryDao.queryById(repeatId)) {
          is Maybe.None -> {
            Timber.w("Could not find repeat for id: $repeatId")
          }
          is Maybe.Data -> {
            val repeat = res.data

            // Just in case my SQL is bad
            if (repeat.archived) {
              Timber.w("Cannot process from archived repeat")
              return@withLock
            }

            if (!repeat.active) {
              Timber.w("Cannot process from inactive repeat")
              return@withLock
            }

            if (repeat.lastRunDay == today) {
              Timber.w("Cannot process from already used repeat today")
              return@withLock
            }

            if (today < repeat.firstDay) {
              Timber.w(
                  "Cannot process repeat not started yet: ${mapOf(
                          "firstDay" to repeat.firstDay,
                          "today" to today
                      )}")
              return@withLock
            }

            // Lock a global mutex so that DB operations only happen once
            try {
              val canCreate =
                  when (repeat.repeatType) {
                    DbRepeat.Type.DAILY -> createTransactionFromDailyRepeat(repeat, today)
                    DbRepeat.Type.WEEKLY_ON_DAY -> createTransactionFromWeeklyRepeat(repeat, today)
                    DbRepeat.Type.MONTHLY_ON_DAY ->
                        createTransactionFromMonthlyRepeat(repeat, today)
                    DbRepeat.Type.YEARLY_ON_DAY -> createTransactionFromYearlyRepeat(repeat, today)
                  }

              if (canCreate) {
                executeRepeat(repeat, today)
              } else {
                Timber.w("Not using repeat: $repeat")
              }
            } catch (e: Throwable) {
              Timber.e(e, "Error during creating transaction from repeat: $repeat")
            }
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
