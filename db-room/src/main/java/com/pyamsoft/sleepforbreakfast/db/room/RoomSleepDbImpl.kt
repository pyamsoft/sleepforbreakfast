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

package com.pyamsoft.sleepforbreakfast.db.room

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import com.pyamsoft.sleepforbreakfast.db.room.automatic.converter.DbAutomaticIdConverter
import com.pyamsoft.sleepforbreakfast.db.room.automatic.entity.RoomDbAutomatic
import com.pyamsoft.sleepforbreakfast.db.room.category.converter.DbCategoryIdConverter
import com.pyamsoft.sleepforbreakfast.db.room.category.entity.RoomDbCategory
import com.pyamsoft.sleepforbreakfast.db.room.converter.LocalDateConverter
import com.pyamsoft.sleepforbreakfast.db.room.converter.LocalDateTimeConverter
import com.pyamsoft.sleepforbreakfast.db.room.transaction.converter.DbCategoriesListConverter
import com.pyamsoft.sleepforbreakfast.db.room.transaction.converter.DbTransactionIdConverter
import com.pyamsoft.sleepforbreakfast.db.room.transaction.converter.DbTransactionTypeConverter
import com.pyamsoft.sleepforbreakfast.db.room.transaction.entity.RoomDbTransaction

@Database(
    exportSchema = true,
    version = 5,
    entities =
        [
            RoomDbTransaction::class,
            RoomDbCategory::class,
            RoomDbAutomatic::class,
        ],
    autoMigrations =
        [
            // Remove this after dev is done and set everything back to 1
            AutoMigration(
                from = 1,
                to = 2,
            ),

            // Remove this after dev is done and set everything back to 1
            AutoMigration(
                from = 2,
                to = 3,
            ),

            // Remove this after dev is done and set everything back to 1
            AutoMigration(
                from = 3,
                to = 4,
            ),

            // Remove this after dev is done and set everything back to 1
            AutoMigration(
                from = 4,
                to = 5,
                spec = DropRepeatVersion4To5::class,
            ),
        ],
)
@TypeConverters(
    // Version 1
    DbCategoriesListConverter::class,
    LocalDateTimeConverter::class,
    LocalDateConverter::class,
    DbTransactionIdConverter::class,
    DbTransactionTypeConverter::class,
    DbCategoryIdConverter::class,
    DbAutomaticIdConverter::class,
)
internal abstract class RoomSleepDbImpl internal constructor() : RoomDatabase(), RoomSleepDb

@DeleteColumn.Entries(
    DeleteColumn(
        tableName = "room_transactions_table",
        columnName = "repeat_id",
    ),
    DeleteColumn(
        tableName = "room_transactions_table",
        columnName = "repeat_date",
    ),
)
@DeleteTable.Entries(
    DeleteTable(
        tableName = "room_repeats_table",
    ),
)
internal class DropRepeatVersion4To5 : AutoMigrationSpec
