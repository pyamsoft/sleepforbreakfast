package com.pyamsoft.sleepforbreakfast.repeat.base

import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import kotlinx.coroutines.CoroutineScope

internal interface LoadRepeatHandler {

  fun loadExistingRepeat(
      scope: CoroutineScope,
      repeatId: DbRepeat.Id,
      onLoaded: (DbRepeat) -> Unit,
  )
}
