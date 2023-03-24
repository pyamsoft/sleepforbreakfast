package com.pyamsoft.sleepforbreakfast.worker.work.repeattransaction

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.core.Maybe
import com.pyamsoft.sleepforbreakfast.db.DbInsert
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.replaceCategories
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
internal class RepeatTransactionHandler
@Inject
internal constructor(
    private val transactionQueryDao: TransactionQueryDao,
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

  private suspend fun insertNewTransaction(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    // Just in case my SQL is bad
    if (repeat.archived) {
      Timber.w("Cannot create transaction from archived repeat")
      return
    }

    if (!repeat.active) {
      Timber.w("Cannot create transaction from inactive repeat")
      return
    }

    // Make a new transaction and put it into the table
    val transaction = createTransaction(repeat, today)

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

  private suspend fun createTransactionFromDailyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    // Make sure there is not already a transaction created for today
    when (val existing = transactionQueryDao.queryByRepeatOnDate(repeat.id, today)) {
      is Maybe.Data -> {
        Timber.w(
            "A transaction already exists for today for the given repeat: ${mapOf(
                      "repeat" to repeat,
                      "transaction" to existing,
                  )}")
      }
      is Maybe.None -> {
        // No additional checks, daily just gets made
        insertNewTransaction(repeat, today)
      }
    }
  }

  private suspend fun createTransactionFromWeeklyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    val mostRecentCreated =
        transactionQueryDao.queryByRepeat(repeat.id).maxByOrNull { it.createdAt }

    // If we have no recent transaction, or the recent transaction is before today,
    // we may be able to
    //
    // else
    //
    // The most recent date is the same before today, we may be able to
    val canCreate: Boolean =
        if (mostRecentCreated == null) true else mostRecentCreated.createdAt.toLocalDate() < today

    if (!canCreate) {
      Timber.d("Not asked to create new weekly transaction, no good")
      return
    }

    // Make sure the day of the week matches the requested,
    // then this should be the "next" week, so make it again
    if (today.dayOfWeek == repeat.firstDate.dayOfWeek) {
      insertNewTransaction(repeat, today)
    }
  }

  private suspend fun createTransactionFromMonthlyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    val mostRecentCreated =
        transactionQueryDao.queryByRepeat(repeat.id).maxByOrNull { it.createdAt }

    var canCreate = false

    // If we have no recent transaction we can create if the day matches
    if (mostRecentCreated == null) {
      canCreate = true
    } else if (mostRecentCreated.createdAt.toLocalDate() < today) {
      // If the most recently created date is before today, maybe

      // The month is last month
      if (mostRecentCreated.createdAt.month < today.month) {
        canCreate = true
      }
    }

    if (!canCreate) {
      Timber.d("Not asked to create new monthly transaction, no good")
      return
    }

    // Make sure the day of the month matches the requested,
    // then this should be the "next" month, so make it again
    if (today.dayOfMonth == repeat.firstDate.dayOfMonth) {
      insertNewTransaction(repeat, today)
    }
  }

  private suspend fun createTransactionFromYearlyRepeat(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    val mostRecentCreated =
        transactionQueryDao.queryByRepeat(repeat.id).maxByOrNull { it.createdAt }

    var canCreate = false

    // If we have no recent transaction we can create if the day matches
    if (mostRecentCreated == null) {
      canCreate = true
    } else if (mostRecentCreated.createdAt.toLocalDate() < today) {
      // If the most recently created date is before today, maybe

      // The month is last month
      if (mostRecentCreated.createdAt.year < today.year) {
        canCreate = true
      }
    }

    if (!canCreate) {
      Timber.d("Not asked to create new yearly transaction, no good")
      return
    }

    // Make sure the day of the year matches the requested,
    // then this should be the "next" month, so make it again
    if (today.dayOfYear == repeat.firstDate.dayOfYear) {
      insertNewTransaction(repeat, today)
    }
  }

  suspend fun process(
      repeat: DbRepeat,
      today: LocalDate,
  ) {
    try {
      when (repeat.repeatType) {
        DbRepeat.Type.DAILY -> createTransactionFromDailyRepeat(repeat, today)
        DbRepeat.Type.WEEKLY_ON_DAY -> createTransactionFromWeeklyRepeat(repeat, today)
        DbRepeat.Type.MONTHLY_ON_DAY -> createTransactionFromMonthlyRepeat(repeat, today)
        DbRepeat.Type.YEARLY_ON_DAY -> createTransactionFromYearlyRepeat(repeat, today)
      }
    } catch (e: Throwable) {
      Timber.e(e, "Error during creating transaction from repeat: $repeat")
    }
  }
}
