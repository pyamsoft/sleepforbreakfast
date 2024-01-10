package com.pyamsoft.sleepforbreakfast.spending.guaranteed

import com.pyamsoft.sleepforbreakfast.db.notification.NotificationInsertDao
import com.pyamsoft.sleepforbreakfast.db.notification.NotificationQueryDao
import com.pyamsoft.sleepforbreakfast.spending.SpendingApi
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class GuaranteedSpending
@Inject
internal constructor(
    private val queryDao: NotificationQueryDao,
    private val insertDao: NotificationInsertDao,
    // Need to use MutableSet instead of Set because of Java -> Kotlin fun.
    @SpendingApi private val guarantees: MutableSet<SpendingGuarantee>,
) {

  suspend fun ensureExistsInDatabase() =
      withContext(context = Dispatchers.Default) {
        guarantees.forEach {
          it.ensureExistsInDatabase(
              query = queryDao,
              insert = insertDao,
          )
        }
      }
}
