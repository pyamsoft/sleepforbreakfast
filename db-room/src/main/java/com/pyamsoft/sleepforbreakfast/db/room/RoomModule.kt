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
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDeleteDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import com.pyamsoft.sleepforbreakfast.db.source.SourceDeleteDao
import com.pyamsoft.sleepforbreakfast.db.source.SourceInsertDao
import com.pyamsoft.sleepforbreakfast.db.source.SourceQueryDao
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

  @Binds @CheckResult internal abstract fun provideDb(impl: SleepDbImpl): SleepDb

  @Module
  companion object {

    private const val DB_NAME = "sleepforbreakfast_room_db.db"

    @Provides
    @JvmStatic
    @CheckResult
    @InternalApi
    internal fun provideRoom(context: Context): RoomSleepDb {
      val appContext = context.applicationContext
      return Room.databaseBuilder(appContext, RoomSleepDbImpl::class.java, DB_NAME).build()
    }

    // DbTransaction
    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomTransactionQueryDao(@InternalApi db: RoomSleepDb): TransactionQueryDao {
      return db.roomTransactionQueryDao
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomTransactionInsertDao(
        @InternalApi db: RoomSleepDb
    ): TransactionInsertDao {
      return db.roomTransactionInsertDao
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomTransactionDeleteDao(
        @InternalApi db: RoomSleepDb
    ): TransactionDeleteDao {
      return db.roomTransactionDeleteDao
    }

    // DbCategory
    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomCategoryQueryDao(@InternalApi db: RoomSleepDb): CategoryQueryDao {
      return db.roomCategoryQueryDao
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomCategoryInsertDao(@InternalApi db: RoomSleepDb): CategoryInsertDao {
      return db.roomCategoryInsertDao
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomCategoryDeleteDao(@InternalApi db: RoomSleepDb): CategoryDeleteDao {
      return db.roomCategoryDeleteDao
    }

    // DbSource
    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomSourceQueryDao(@InternalApi db: RoomSleepDb): SourceQueryDao {
      return db.roomSourceQueryDao
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomSourceInsertDao(@InternalApi db: RoomSleepDb): SourceInsertDao {
      return db.roomSourceInsertDao
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomSourceDeleteDao(@InternalApi db: RoomSleepDb): SourceDeleteDao {
      return db.roomSourceDeleteDao
    }

    // DbRepeat
    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomRepeatQueryDao(@InternalApi db: RoomSleepDb): RepeatQueryDao {
      return db.roomRepeatQueryDao
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomRepeatInsertDao(@InternalApi db: RoomSleepDb): RepeatInsertDao {
      return db.roomRepeatInsertDao
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomRepeatDeleteDao(@InternalApi db: RoomSleepDb): RepeatDeleteDao {
      return db.roomRepeatDeleteDao
    }

    // DbAutomatic
    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomAutomaticQueryDao(@InternalApi db: RoomSleepDb): AutomaticQueryDao {
      return db.roomAutomaticQueryDao
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomAutomaticInsertDao(@InternalApi db: RoomSleepDb): AutomaticInsertDao {
      return db.roomAutomaticInsertDao
    }

    @DbApi
    @Provides
    @JvmStatic
    internal fun provideRoomAutomaticDeleteDao(@InternalApi db: RoomSleepDb): AutomaticDeleteDao {
      return db.roomAutomaticDeleteDao
    }
  }
}
