package com.pyamsoft.sleepforbreakfast.db

import androidx.annotation.CheckResult
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticDb
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticDbImpl
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticDeleteDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticInsertDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticQueryDao
import com.pyamsoft.sleepforbreakfast.db.automatic.AutomaticRealtime
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDb
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDbImpl
import com.pyamsoft.sleepforbreakfast.db.category.CategoryDeleteDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryInsertDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryQueryDao
import com.pyamsoft.sleepforbreakfast.db.category.CategoryRealtime
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDb
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDbImpl
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatDeleteDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatInsertDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatQueryDao
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatRealtime
import com.pyamsoft.sleepforbreakfast.db.source.SourceDb
import com.pyamsoft.sleepforbreakfast.db.source.SourceDbImpl
import com.pyamsoft.sleepforbreakfast.db.source.SourceDeleteDao
import com.pyamsoft.sleepforbreakfast.db.source.SourceInsertDao
import com.pyamsoft.sleepforbreakfast.db.source.SourceQueryDao
import com.pyamsoft.sleepforbreakfast.db.source.SourceRealtime
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDb
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDbImpl
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionDeleteDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionInsertDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionQueryDao
import com.pyamsoft.sleepforbreakfast.db.transaction.TransactionRealtime
import dagger.Binds
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier

@Qualifier @Retention(AnnotationRetention.BINARY) private annotation class InternalApi

@Module
abstract class DbModule {

  // DB
  @Binds
  @CheckResult
  internal abstract fun provideTransactionDbImpl(impl: TransactionDbImpl): TransactionDb

  @Binds @CheckResult internal abstract fun provideCategoryDbImpl(impl: CategoryDbImpl): CategoryDb

  @Binds @CheckResult internal abstract fun provideSourceDbImpl(impl: SourceDbImpl): SourceDb

  @Binds @CheckResult internal abstract fun provideRepeatDbImpl(impl: RepeatDbImpl): RepeatDb

  @Binds
  @CheckResult
  internal abstract fun provideAutomaticDbImpl(impl: AutomaticDbImpl): AutomaticDb

  // Caches
  @Binds
  @CheckResult
  internal abstract fun provideTransactionCache(impl: TransactionDbImpl): TransactionQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun provideCategoryCache(impl: CategoryDbImpl): CategoryQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun provideSourceCache(impl: SourceDbImpl): SourceQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun provideRepeatCache(impl: RepeatDbImpl): RepeatQueryDao.Cache

  @Binds
  @CheckResult
  internal abstract fun provideAutomaticCache(impl: AutomaticDbImpl): AutomaticQueryDao.Cache

  @Module
  companion object {

    // DbTransaction
    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideTransactionDb(db: SleepDb): TransactionDb {
      return db.transactions
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideTransactionRealtimeDao(
        @InternalApi db: TransactionDb
    ): TransactionRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideTransactionQueryDao(@InternalApi db: TransactionDb): TransactionQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideTransactionInsertDao(@InternalApi db: TransactionDb): TransactionInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideTransactionDeleteDao(@InternalApi db: TransactionDb): TransactionDeleteDao {
      return db.deleteDao
    }

    // DbCategory
    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideCategoryDb(db: SleepDb): CategoryDb {
      return db.categories
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideCategoryRealtimeDao(@InternalApi db: CategoryDb): CategoryRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideCategoryQueryDao(@InternalApi db: CategoryDb): CategoryQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideCategoryInsertDao(@InternalApi db: CategoryDb): CategoryInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideCategoryDeleteDao(@InternalApi db: CategoryDb): CategoryDeleteDao {
      return db.deleteDao
    }

    // DbSource
    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideSourceDb(db: SleepDb): SourceDb {
      return db.sources
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSourceRealtimeDao(@InternalApi db: SourceDb): SourceRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSourceQueryDao(@InternalApi db: SourceDb): SourceQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSourceInsertDao(@InternalApi db: SourceDb): SourceInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideSourceDeleteDao(@InternalApi db: SourceDb): SourceDeleteDao {
      return db.deleteDao
    }

    // DbRepeat
    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideRepeatDb(db: SleepDb): RepeatDb {
      return db.repeats
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideRepeatRealtimeDao(@InternalApi db: RepeatDb): RepeatRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideRepeatQueryDao(@InternalApi db: RepeatDb): RepeatQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideRepeatInsertDao(@InternalApi db: RepeatDb): RepeatInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideRepeatDeleteDao(@InternalApi db: RepeatDb): RepeatDeleteDao {
      return db.deleteDao
    }

    // DbAutomatic
    @JvmStatic
    @Provides
    @CheckResult
    @InternalApi
    internal fun provideAutomaticDb(db: SleepDb): AutomaticDb {
      return db.automatics
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideAutomaticRealtimeDao(@InternalApi db: AutomaticDb): AutomaticRealtime {
      return db.realtime
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideAutomaticQueryDao(@InternalApi db: AutomaticDb): AutomaticQueryDao {
      return db.queryDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideAutomaticInsertDao(@InternalApi db: AutomaticDb): AutomaticInsertDao {
      return db.insertDao
    }

    @JvmStatic
    @Provides
    @CheckResult
    internal fun provideAutomaticDeleteDao(@InternalApi db: AutomaticDb): AutomaticDeleteDao {
      return db.deleteDao
    }
  }
}
