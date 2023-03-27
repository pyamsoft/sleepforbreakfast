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

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.room.automatic.dao.RoomAutomaticDeleteDao
import com.pyamsoft.sleepforbreakfast.db.room.automatic.dao.RoomAutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.room.automatic.dao.RoomAutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.room.category.dao.RoomCategoryDeleteDao
import com.pyamsoft.sleepforbreakfast.db.room.category.dao.RoomCategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.room.category.dao.RoomCategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.room.repeat.dao.RoomRepeatDeleteDao
import com.pyamsoft.sleepforbreakfast.db.room.repeat.dao.RoomRepeatInsertDao
import com.pyamsoft.sleepforbreakfast.db.room.repeat.dao.RoomRepeatQueryDao
import com.pyamsoft.sleepforbreakfast.db.room.transaction.dao.RoomTransactionDeleteDao
import com.pyamsoft.sleepforbreakfast.db.room.transaction.dao.RoomTransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.room.transaction.dao.RoomTransactionQueryDao

internal interface RoomSleepDb {

  // Transactions
  @get:CheckResult val roomTransactionQueryDao: RoomTransactionQueryDao
  @get:CheckResult val roomTransactionInsertDao: RoomTransactionInsertDao
  @get:CheckResult val roomTransactionDeleteDao: RoomTransactionDeleteDao

  // Category
  @get:CheckResult val roomCategoryQueryDao: RoomCategoryQueryDao
  @get:CheckResult val roomCategoryInsertDao: RoomCategoryInsertDao
  @get:CheckResult val roomCategoryDeleteDao: RoomCategoryDeleteDao

  // Repeat
  @get:CheckResult val roomRepeatQueryDao: RoomRepeatQueryDao
  @get:CheckResult val roomRepeatInsertDao: RoomRepeatInsertDao
  @get:CheckResult val roomRepeatDeleteDao: RoomRepeatDeleteDao

  // Automatic
  @get:CheckResult val roomAutomaticQueryDao: RoomAutomaticQueryDao
  @get:CheckResult val roomAutomaticInsertDao: RoomAutomaticInsertDao
  @get:CheckResult val roomAutomaticDeleteDao: RoomAutomaticDeleteDao
}
