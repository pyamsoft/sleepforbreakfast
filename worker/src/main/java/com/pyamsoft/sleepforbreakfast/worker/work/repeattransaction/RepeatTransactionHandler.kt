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
import kotlinx.coroutines.Deferred
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

  private suspend fun markRepeatUsed(repeat: DbRepeat, date: LocalDate) {
    when (val res = repeatInsertDao.insert(repeat.lastRunDay(date))) {
      is DbInsert.InsertResult.Fail -> {
        Timber.e(
            res.error,
            "Failed updating Repeat to lastUsed: ${mapOf(
                      "repeat" to repeat,
                      "date" to date,
                  )}")
      }
      is DbInsert.InsertResult.Insert -> {
        Timber.d(
            "Inserted new repeat, should this happen?: ${mapOf(
                      "repeat" to repeat,
                      "date" to date,
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
      date: LocalDate,
  ) {
    // Make a new transaction and put it into the table
    val transaction =
        createTransaction(
            repeat = repeat,
            date = date,
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

  private suspend fun executeRepeat(
      repeat: DbRepeat,
      dates: List<LocalDate>,
      lastUsed: LocalDate,
  ) = coroutineScope {
    val jobs =
        mutableListOf<Deferred<*>>(
            async(context = Dispatchers.Default) { markRepeatUsed(repeat, lastUsed) },
        )

    for (d in dates) {
      jobs.add(
          async(context = Dispatchers.Default) { insertNewTransaction(repeat, d) },
      )
    }

    jobs.awaitAll()
  }

  @CheckResult
  private fun createTransactionFromDailyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): List<LocalDate> {
    val mostRecentCreated = repeat.lastRunDay

    val dates = mutableListOf<LocalDate>()
    // If we have no previous runs, this is brand new
    if (mostRecentCreated == null) {
      // Fill in the dates we missed from first date until today
      var cursorDate = repeat.firstDay
      while (cursorDate < today) {
        dates.add(cursorDate)
        Timber.d(
            "Fill-in DAILY repeat: ${mapOf(
              "cursor" to cursorDate,
              "today" to today,
          )}")
        cursorDate = cursorDate.plusDays(1)
      }
    } else {
      // Otherwise, we check that
      // The most recently created date was yesterday
      val previousWasYesterday = mostRecentCreated.dayOfYear < today.dayOfYear
      if (previousWasYesterday) {
        Timber.d("Create new DAILY repeat for today: $today")
        dates.add(today)
      } else {
        Timber.w(
            "Not running DAILY repeat, days don't match up ${mapOf(
                "today" to today,
                "firstDay" to repeat.firstDay,
                "mostRecent" to mostRecentCreated,
            )}")
      }
    }

    return dates
  }

  @CheckResult
  private fun createTransactionFromWeeklyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): List<LocalDate> {
    val mostRecentCreated = repeat.lastRunDay

    val dates = mutableListOf<LocalDate>()
    // If we have no previous runs, this is brand new
    if (mostRecentCreated == null) {
      // Fill in the dates we missed from first date until today
      var cursorDate = repeat.firstDay
      while (cursorDate < today && cursorDate.dayOfWeek == today.dayOfWeek) {
        dates.add(cursorDate)
        Timber.d(
            "Fill-in WEEKLY repeat: ${mapOf(
                "cursor" to cursorDate,
                "today" to today,
            )}")
        cursorDate = cursorDate.plusWeeks(1)
      }
    } else {
      // Otherwise, we check that
      // The most recently created date was last week
      // and the date today matches the "loop" date
      val previousWasLastWeek = mostRecentCreated.dayOfMonth < today.dayOfMonth
      val isCurrentDay = today.dayOfWeek == repeat.firstDay.dayOfWeek
      if (previousWasLastWeek && isCurrentDay) {
        Timber.d("Create new WEEKLY repeat for today: $today")
        dates.add(today)
      } else {
        Timber.w(
            "Not running WEEKLY repeat, days don't match up ${mapOf(
                "previousWasLastWeek" to previousWasLastWeek,
                "today" to today,
                "firstDay" to repeat.firstDay,
                "dayOfWeek" to repeat.firstDay.dayOfWeek,
                "mostRecent" to mostRecentCreated,
            )}")
      }
    }

    return dates
  }

  @CheckResult
  private fun createTransactionFromMonthlyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): List<LocalDate> {
    val mostRecentCreated = repeat.lastRunDay

    val dates = mutableListOf<LocalDate>()
    // If we have no previous runs, this is brand new
    if (mostRecentCreated == null) {
      // Fill in the dates we missed from first date until today
      var cursorDate = repeat.firstDay
      while (cursorDate < today && cursorDate.dayOfMonth == today.dayOfMonth) {
        dates.add(cursorDate)
        Timber.d(
            "Fill-in MONTHLY repeat: ${mapOf(
                    "cursor" to cursorDate,
                    "today" to today,
                )}")
        cursorDate = cursorDate.plusMonths(1)
      }
    } else {
      // Otherwise, we check that
      // The most recently created date was last month
      // and the date today matches the "loop" date
      val previousWasLastMonth = mostRecentCreated.month < today.month
      val isCurrentDay = today.dayOfMonth == repeat.firstDay.dayOfMonth
      if (previousWasLastMonth && isCurrentDay) {
        Timber.d("Create new MONTHLY repeat for today: $today")
        dates.add(today)
      } else {
        Timber.w(
            "Not running MONTHLY repeat, days don't match up ${mapOf(
                  "previousWasLastMonth" to previousWasLastMonth,
                  "isCurrentDay" to isCurrentDay,
                  "today" to today,
                  "firstDay" to repeat.firstDay,
                  "dayOfMonth" to repeat.firstDay.dayOfMonth,
                "mostRecent" to mostRecentCreated,
              )}")
      }
    }

    return dates
  }

  @CheckResult
  private fun createTransactionFromYearlyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ): List<LocalDate> {
    val mostRecentCreated = repeat.lastRunDay

    // If we have no recent transaction we can create if the day matches
    val dates = mutableListOf<LocalDate>()

    // If we have no previous runs, this is brand new
    if (mostRecentCreated == null) {
      // Fill in the dates we missed from first date until today
      var cursorDate = repeat.firstDay
      while (cursorDate < today && cursorDate.dayOfYear == today.dayOfYear) {
        dates.add(cursorDate)
        Timber.d(
            "Fill-in YEAR repeat: ${mapOf(
                    "cursor" to cursorDate,
                    "today" to today,
                )}")
        cursorDate = cursorDate.plusYears(1)
      }
    } else {
      // Otherwise, we check that
      // The most recently created date was last year
      // and the date today matches the "loop" date
      val previousWasLastYear = mostRecentCreated.year < today.year
      val isCurrentDay = today.dayOfYear == repeat.firstDay.dayOfYear
      if (previousWasLastYear && isCurrentDay) {
        Timber.d("Create new YEARLY repeat for today: $today")
        dates.add(today)
      } else {
        Timber.w(
            "Not running YEARLY repeat, days don't match up ${mapOf(
              "previousWasLastYear" to previousWasLastYear,
              "isCurrentDay" to isCurrentDay,
              "today" to today,
              "firstDay" to repeat.firstDay,
              "dayOfYear" to repeat.firstDay.dayOfYear,
              "mostRecent" to mostRecentCreated,
          )}")
      }
    }

    return emptyList()
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
              val dates =
                  when (repeat.repeatType) {
                    DbRepeat.Type.DAILY -> createTransactionFromDailyRepeat(repeat, today)
                    DbRepeat.Type.WEEKLY_ON_DAY -> createTransactionFromWeeklyRepeat(repeat, today)
                    DbRepeat.Type.MONTHLY_ON_DAY ->
                        createTransactionFromMonthlyRepeat(repeat, today)
                    DbRepeat.Type.YEARLY_ON_DAY -> createTransactionFromYearlyRepeat(repeat, today)
                  }

              if (dates.isNotEmpty()) {
                executeRepeat(
                    repeat = repeat,
                    dates = dates,
                    lastUsed = today,
                )
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
