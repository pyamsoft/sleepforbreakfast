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

package com.pyamsoft.sleepforbreakfast.db.room.transaction.entity

import androidx.annotation.CheckResult
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.pyamsoft.sleepforbreakfast.db.automatic.DbAutomatic
import com.pyamsoft.sleepforbreakfast.db.category.DbCategory
import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.room.automatic.entity.RoomDbAutomatic
import com.pyamsoft.sleepforbreakfast.db.room.repeat.entity.RoomDbRepeat
import com.pyamsoft.sleepforbreakfast.db.room.source.entity.RoomDbSource
import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import java.time.LocalDateTime

@Entity(
    tableName = RoomDbTransaction.TABLE_NAME,
    foreignKeys =
        [
            // Link DbAutomatic entries to the Transactions they create
            ForeignKey(
                entity = RoomDbAutomatic::class,
                parentColumns = [RoomDbAutomatic.COLUMN_ID],
                childColumns = [RoomDbTransaction.COLUMN_AUTOMATIC_ID],
                onDelete = ForeignKey.CASCADE,
            ),

            // Link DbRepeat entries to the Transactions they create
            ForeignKey(
                entity = RoomDbRepeat::class,
                parentColumns = [RoomDbRepeat.COLUMN_ID],
                childColumns = [RoomDbTransaction.COLUMN_REPEAT_ID],
                onDelete = ForeignKey.CASCADE,
            ),

            // Link DbSource entries to the Transactions they create
            ForeignKey(
                entity = RoomDbSource::class,
                parentColumns = [RoomDbSource.COLUMN_ID],
                childColumns = [RoomDbTransaction.COLUMN_SOURCE_ID],
                onDelete = ForeignKey.CASCADE,
            ),

            // TODO Peter: How do we FK on many category IDs?
        ],
)
internal data class RoomDbTransaction
internal constructor(
    @JvmField @PrimaryKey @ColumnInfo(name = COLUMN_ID) val dbId: DbTransaction.Id,
    @JvmField @ColumnInfo(name = COLUMN_SOURCE_ID, index = true) val dbSourceId: DbSource.Id?,
    @JvmField @ColumnInfo(name = COLUMN_CATEGORY_ID) val dbCategories: List<DbCategory.Id>,
    @JvmField @ColumnInfo(name = COLUMN_NAME) val dbName: String,
    @JvmField @ColumnInfo(name = COLUMN_TYPE) val dbType: DbTransaction.Type,
    @JvmField @ColumnInfo(name = COLUMN_AMOUNT_IN_CENTS) val dbAmountInCents: Long,
    @JvmField @ColumnInfo(name = COLUMN_DATE) val dbDate: LocalDateTime,
    @JvmField @ColumnInfo(name = COLUMN_NOTE) val dbNote: String,
    @JvmField @ColumnInfo(name = COLUMN_REPEAT_ID, index = true) val dbRepeatId: DbRepeat.Id?,
    @JvmField
    @ColumnInfo(name = COLUMN_AUTOMATIC_ID, index = true)
    val dbAutomaticId: DbAutomatic.Id?,
) : DbTransaction {

  @Ignore override val id = dbId

  @Ignore override val sourceId = dbSourceId

  @Ignore override val categories = dbCategories

  @Ignore override val name = dbName

  @Ignore override val type = dbType

  @Ignore override val amountInCents = dbAmountInCents

  @Ignore override val date = dbDate

  @Ignore override val note = dbNote

  @Ignore override val repeatId = dbRepeatId

  @Ignore override val automaticId = dbAutomaticId

  @Ignore
  override fun sourceId(id: DbSource.Id): DbTransaction {
    return this.copy(dbSourceId = id)
  }

  @Ignore
  override fun removeSourceId(): DbTransaction {
    return this.copy(dbSourceId = null)
  }

  @Ignore
  override fun addCategory(id: DbCategory.Id): DbTransaction {
    return this.copy(dbCategories = this.dbCategories + id)
  }

  @Ignore
  override fun removeCategory(id: DbCategory.Id): DbTransaction {
    return this.copy(dbCategories = this.dbCategories.filterNot { it == id })
  }

  @Ignore
  override fun clearCategories(): DbTransaction {
    return this.copy(dbCategories = emptyList())
  }

  @Ignore
  override fun name(name: String): DbTransaction {
    return this.copy(dbName = name)
  }

  @Ignore
  override fun type(type: DbTransaction.Type): DbTransaction {
    return this.copy(dbType = type)
  }

  @Ignore
  override fun amountInCents(amountInCents: Long): DbTransaction {
    return this.copy(dbAmountInCents = amountInCents)
  }

  @Ignore
  override fun date(date: LocalDateTime): DbTransaction {
    return this.copy(dbDate = date)
  }

  @Ignore
  override fun note(note: String): DbTransaction {
    return this.copy(dbNote = note)
  }

  @Ignore
  override fun repeatId(id: DbRepeat.Id): DbTransaction {
    return this.copy(dbRepeatId = id)
  }

  @Ignore
  override fun automaticId(id: DbAutomatic.Id): DbTransaction {
    return this.copy(dbAutomaticId = id)
  }

  companion object {

    @Ignore internal const val TABLE_NAME = "room_transactions_table"

    @Ignore internal const val COLUMN_ID = "_id"

    @Ignore internal const val COLUMN_SOURCE_ID = "source_id"

    @Ignore internal const val COLUMN_CATEGORY_ID = "category_id"

    @Ignore internal const val COLUMN_NAME = "name"

    @Ignore internal const val COLUMN_TYPE = "type"

    @Ignore internal const val COLUMN_AMOUNT_IN_CENTS = "amount_in_cents"

    @Ignore internal const val COLUMN_DATE = "date"

    @Ignore internal const val COLUMN_NOTE = "note"

    @Ignore internal const val COLUMN_REPEAT_ID = "repeat_id"

    @Ignore internal const val COLUMN_AUTOMATIC_ID = "automatic_id"

    @Ignore
    @JvmStatic
    @CheckResult
    internal fun create(item: DbTransaction): RoomDbTransaction {
      return if (item is RoomDbTransaction) item
      else {
        RoomDbTransaction(
            item.id,
            item.sourceId,
            item.categories,
            item.name,
            item.type,
            item.amountInCents,
            item.date,
            item.note,
            item.repeatId,
            item.automaticId,
        )
      }
    }
  }
}
