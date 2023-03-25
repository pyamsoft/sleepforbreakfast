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

package com.pyamsoft.sleepforbreakfast.db.room.devonly

import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
import com.pyamsoft.sleepforbreakfast.db.room.category.entity.RoomDbCategory
import com.pyamsoft.sleepforbreakfast.db.room.repeat.entity.RoomDbRepeat
import com.pyamsoft.sleepforbreakfast.db.room.transaction.entity.RoomDbTransaction

@DeleteColumn.Entries(
    DeleteColumn(
        tableName = RoomDbTransaction.TABLE_NAME,
        columnName = "source_id",
    ),
    DeleteColumn(
        tableName = RoomDbRepeat.TABLE_NAME,
        columnName = "transaction_source_id",
    ),
)
@DeleteTable.Entries(
    DeleteTable(
        tableName = "room_categories_table",
    ),
    DeleteTable(
        tableName = "room_sources_table",
    ),
)
class AutoMigrate3To4 : AutoMigrationSpec

@DeleteColumn.Entries(
    DeleteColumn(
        tableName = RoomDbCategory.TABLE_NAME,
        columnName = "account",
    ),
)
class AutoMigrate4To5 : AutoMigrationSpec
