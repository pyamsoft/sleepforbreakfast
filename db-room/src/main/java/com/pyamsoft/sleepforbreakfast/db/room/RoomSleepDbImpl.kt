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

package com.pyamsoft.sleepforbreakfast.db.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pyamsoft.sleepforbreakfast.db.room.category.converter.DbCategoryIdConverter
import com.pyamsoft.sleepforbreakfast.db.room.category.entity.RoomDbCategory
import com.pyamsoft.sleepforbreakfast.db.room.source.converter.DbSourceIdConverter
import com.pyamsoft.sleepforbreakfast.db.room.source.entity.RoomDbSource
import com.pyamsoft.sleepforbreakfast.db.room.transaction.converter.DbTransactionCategoriesConverter
import com.pyamsoft.sleepforbreakfast.db.room.transaction.converter.DbTransactionDateConverter
import com.pyamsoft.sleepforbreakfast.db.room.transaction.converter.DbTransactionIdConverter
import com.pyamsoft.sleepforbreakfast.db.room.transaction.converter.DbTransactionTypeConverter
import com.pyamsoft.sleepforbreakfast.db.room.transaction.entity.RoomDbTransaction

@Database(
    exportSchema = true,
    version = 1,
    entities =
        [
            // Version 1
            RoomDbTransaction::class,
            RoomDbCategory::class,
            RoomDbSource::class,
        ],
    autoMigrations = [],
)
@TypeConverters(
    // Version 1
    DbTransactionIdConverter::class,
    DbTransactionTypeConverter::class,
    DbTransactionDateConverter::class,
    DbTransactionCategoriesConverter::class,
    DbSourceIdConverter::class,
    DbCategoryIdConverter::class,
)
internal abstract class RoomSleepDbImpl internal constructor() : RoomDatabase(), RoomSleepDb
