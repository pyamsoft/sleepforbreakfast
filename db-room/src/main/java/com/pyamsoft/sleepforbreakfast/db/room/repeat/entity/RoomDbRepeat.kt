/*
 * Copyright 2021 Peter Kenji Yamanaka
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

package com.pyamsoft.sleepforbreakfast.db.room.repeat.entity

import androidx.annotation.CheckResult
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import java.time.LocalDate

@Entity(tableName = RoomDbRepeat.TABLE_NAME)
internal data class RoomDbRepeat
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbRepeat.Id,
    @JvmField
    @ColumnInfo(name = COLUMN_TRANSACTION_SOURCE_ID)
    val dbTransactionSourceId: DbSource.Id?,
    @JvmField
    @ColumnInfo(name = COLUMN_TRANSACTION_CATEGORY_ID)
    val dbTransactionCategories: List<DbCategory.Id>,
    @JvmField @ColumnInfo(name = COLUMN_TRANSACTION_NAME) val dbTransactionName: String,
    @JvmField @ColumnInfo(name = COLUMN_TRANSACTION_TYPE) val dbTransactionType: DbTransaction.Type,
    @JvmField
    @ColumnInfo(name = COLUMN_TRANSACTION_AMOUNT_IN_CENTS)
    val dbTransactionAmountInCents: Long,
    @JvmField @ColumnInfo(name = COLUMN_TRANSACTION_NOTE) val dbTransactionNote: String,
    @JvmField @ColumnInfo(name = COLUMN_REPEAT_TYPE) val dbRepeatType: DbRepeat.Type,
    @JvmField @ColumnInfo(name = COLUMN_FIRST_DAY) val dbFirstDay: LocalDate,
    @JvmField @ColumnInfo(name = COLUMN_ACTIVE) val dbActive: Boolean,
    @JvmField @ColumnInfo(name = COLUMN_ARCHIVED) val dbArchived: Boolean,
) : DbRepeat {

  @Ignore override val id = dbId

  @Ignore override val transactionSourceId = dbTransactionSourceId

  @Ignore override val transactionCategories = dbTransactionCategories

  @Ignore override val transactionName = dbTransactionName

  @Ignore override val transactionAmountInCents = dbTransactionAmountInCents

  @Ignore override val transactionType = dbTransactionType

  @Ignore override val transactionNote = dbTransactionNote

  @Ignore override val repeatType = dbRepeatType

  @Ignore override val firstDate = dbFirstDay

  @Ignore override val active = dbActive

  @Ignore override val archived = dbArchived

  @Ignore
  override fun transactionSourceId(id: DbSource.Id): DbRepeat {
    return this.copy(dbTransactionSourceId = id)
  }

  @Ignore
  override fun removeTransactionSourceId(): DbRepeat {
    return this.copy(dbTransactionSourceId = null)
  }

  @Ignore
  override fun addTransactionCategory(id: DbCategory.Id): DbRepeat {
    return this.copy(dbTransactionCategories = this.dbTransactionCategories + id)
  }

  @Ignore
  override fun removeTransactionCategory(id: DbCategory.Id): DbRepeat {
    return this.copy(dbTransactionCategories = this.dbTransactionCategories.filterNot { it == id })
  }

  @Ignore
  override fun clearTransactionCategories(): DbRepeat {
    return this.copy(dbTransactionCategories = emptyList())
  }

  @Ignore
  override fun transactionName(name: String): DbRepeat {
    return this.copy(dbTransactionName = name)
  }

  @Ignore
  override fun transactionAmountInCents(amountInCents: Long): DbRepeat {
    return this.copy(dbTransactionAmountInCents = amountInCents)
  }

  @Ignore
  override fun transactionType(type: DbTransaction.Type): DbRepeat {
    return this.copy(dbTransactionType = type)
  }

  @Ignore
  override fun transactionNote(note: String): DbRepeat {
    return this.copy(dbTransactionNote = note)
  }

  @Ignore
  override fun repeatType(type: DbRepeat.Type): DbRepeat {
    return this.copy(dbRepeatType = type)
  }

  @Ignore
  override fun firstDay(date: LocalDate): DbRepeat {
    return this.copy(dbFirstDay = date)
  }

  @Ignore
  override fun activate(): DbRepeat {
    return this.copy(dbActive = true)
  }

  @Ignore
  override fun deactivate(): DbRepeat {
    return this.copy(dbActive = false)
  }

  @Ignore
  override fun archive(): DbRepeat {
    return this.copy(dbArchived = true)
  }

  @Ignore
  override fun unarchive(): DbRepeat {
    return this.copy(dbArchived = false)
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_repeats_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_TRANSACTION_SOURCE_ID = "transaction_source_id"

    @Ignore internal const val COLUMN_TRANSACTION_CATEGORY_ID = "transaction_category_id"

    @Ignore internal const val COLUMN_TRANSACTION_NAME = "transaction_name"

    @Ignore internal const val COLUMN_TRANSACTION_TYPE = "transaction_type"

    @Ignore internal const val COLUMN_TRANSACTION_AMOUNT_IN_CENTS = "transaction_amount_in_cents"

    @Ignore internal const val COLUMN_TRANSACTION_NOTE = "transaction_note"

    @Ignore internal const val COLUMN_REPEAT_TYPE = "repeat_type"

    @Ignore internal const val COLUMN_FIRST_DAY = "first_day"

    @Ignore internal const val COLUMN_ACTIVE = "active"

    @Ignore internal const val COLUMN_ARCHIVED = "archived"

    @Ignore internal const val V2_COLUMN_REPEAT_DATE = "repeat_date"

    @Ignore internal const val V3_COLUMN_REPEAT_TIME = "repeat_time"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbRepeat): RoomDbRepeat {
      return if (item is RoomDbRepeat) item
      else {
        RoomDbRepeat(
            item.id,
            item.transactionSourceId,
            item.transactionCategories,
            item.transactionName,
            item.transactionType,
            item.transactionAmountInCents,
            item.transactionNote,
            item.repeatType,
            item.firstDate,
            item.active,
            item.archived,
        )
      }
    }
  }
}
