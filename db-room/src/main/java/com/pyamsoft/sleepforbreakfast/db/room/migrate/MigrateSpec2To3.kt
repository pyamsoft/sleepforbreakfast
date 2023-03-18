package com.pyamsoft.sleepforbreakfast.db.room.migrate

import androidx.room.DeleteColumn
import androidx.room.migration.AutoMigrationSpec
import com.pyamsoft.sleepforbreakfast.db.room.repeat.entity.RoomDbRepeat

/**
 * From v2 to v3
 *
 * We drop the repeatDate column
 */
@DeleteColumn(
    tableName = RoomDbRepeat.TABLE_NAME,
    columnName = RoomDbRepeat.V2_COLUMN_REPEAT_DATE,
)
internal class MigrateSpec2To3 : AutoMigrationSpec
