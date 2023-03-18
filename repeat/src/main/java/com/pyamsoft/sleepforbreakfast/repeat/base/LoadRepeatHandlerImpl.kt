package com.pyamsoft.sleepforbreakfast.repeat.base

import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

@Singleton
class LoadRepeatHandlerImpl
@Inject
internal constructor(
    private val interactor: LoadRepeatInteractor,
) : LoadRepeatHandler {

  override fun loadExistingRepeat(
      scope: CoroutineScope,
      repeatId: DbRepeat.Id,
      onLoaded: (DbRepeat) -> Unit,
  ) {
    if (!repeatId.isEmpty) {
      scope.launch(context = Dispatchers.Main) {
        interactor
            .load(repeatId)
            .onSuccess { repeat ->
              Timber.d("Loaded repeat for editing: $repeat")
              onLoaded(repeat)
            }
            .onFailure { Timber.e(it, "Error loading repeat for editing: $repeatId") }
      }
    }
  }
}
