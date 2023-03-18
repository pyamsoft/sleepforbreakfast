package com.pyamsoft.sleepforbreakfast.transactions.base

import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
class LoadTransactionHandlerImpl
@Inject
internal constructor(
    private val interactor: LoadTransactionInteractor,
) : LoadTransactionHandler {

  override fun loadExistingTransaction(
      scope: CoroutineScope,
      transactionId: DbTransaction.Id,
      onLoaded: (DbTransaction) -> Unit,
  ) {
    if (!transactionId.isEmpty) {
      scope.launch(context = Dispatchers.Main) {
        interactor
            .load(transactionId = transactionId)
            .onSuccess { transaction ->
              Timber.d("Loaded transaction for editing: $transaction")
              onLoaded(transaction)
            }
            .onFailure { Timber.e(it, "Error loading transaction for editing: $transactionId") }
      }
    }
  }
}
