package com.pyamsoft.sleepforbreakfast.db.room.devonly

import androidx.room.DeleteColumn
import androidx.room.DeleteTable
import androidx.room.migration.AutoMigrationSpec
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
