/*
 * Copyright 2025 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
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

import android.content.Context
import androidx.annotation.CheckResult
import androidx.room.Room
import com.pyamsoft.sleepforbreakfast.db.DbApi
import com.pyamsoft.sleepforbreakfast.db.SleepDb
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticDeleteDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDeleteDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationDeleteDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationInsertDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDeleteDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) private annotation class InternalApi

@Module
abstract class RoomModule {

  @Binds @CheckResult internal abstract fun bindDb(impl: SleepDbImpl): SleepDb

  @Binds
  @CheckResult
  @InternalApi
  internal abstract fun bindRoomDb(@InternalApi impl: RoomSleepDbImpl): RoomSleepDb

  @Module
  companion object {

    private const val DB_NAME = "sleepforbreakfast_room_db.db"

    @Provides
    @JvmStatic
    @CheckResult
    @InternalApi
    internal fun provideRoom(context: Context): RoomSleepDbImpl {
      val appContext = context.applicationContext
      return Room.databaseBuilder(appContext, RoomSleepDbImpl::class.java, DB_NAME).build()
    }

    // DbTransaction
    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomTransactionQueryDao(@InternalApi db: RoomSleepDb): TransactionQueryDao {
      return db.roomTransactionQueryDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomTransactionInsertDao(
        @InternalApi db: RoomSleepDb
    ): TransactionInsertDao {
      return db.roomTransactionInsertDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomTransactionDeleteDao(
        @InternalApi db: RoomSleepDb
    ): TransactionDeleteDao {
      return db.roomTransactionDeleteDao()
    }

    // DbCategory
    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomCategoryQueryDao(@InternalApi db: RoomSleepDb): CategoryQueryDao {
      return db.roomCategoryQueryDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomCategoryInsertDao(@InternalApi db: RoomSleepDb): CategoryInsertDao {
      return db.roomCategoryInsertDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomCategoryDeleteDao(@InternalApi db: RoomSleepDb): CategoryDeleteDao {
      return db.roomCategoryDeleteDao()
    }

    // DbAutomatic
    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomAutomaticQueryDao(@InternalApi db: RoomSleepDb): AutomaticQueryDao {
      return db.roomAutomaticQueryDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomAutomaticInsertDao(@InternalApi db: RoomSleepDb): AutomaticInsertDao {
      return db.roomAutomaticInsertDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomAutomaticDeleteDao(@InternalApi db: RoomSleepDb): AutomaticDeleteDao {
      return db.roomAutomaticDeleteDao()
    }

    // DbNotification
    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomNotificationQueryDao(
        @InternalApi db: RoomSleepDb
    ): NotificationQueryDao {
      return db.roomNotificationQueryDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomNotificationInsertDao(
        @InternalApi db: RoomSleepDb
    ): NotificationInsertDao {
      return db.roomNotificationInsertDao()
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomNotificationDeleteDao(
        @InternalApi db: RoomSleepDb
    ): NotificationDeleteDao {
      return db.roomNotificationDeleteDao()
    }
  }
}
