package com.pyamsoft.sleepforbreakfast.transactions.base

import com.pyamsoft.pydroid.arch.AbstractViewModeler
import com.pyamsoft.pydroid.arch.UiViewState
import com.pyamsoft.sleepforbreakfast.db.transaction.DbTransaction
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

abstract class SingleViewModeler<S : UiViewState>
protected constructor(
    override val state: S,
    private val interactor: SingleTransactionInteractor,
) : AbstractViewModeler<S>(state) {

  protected fun loadExistingTransaction(
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
