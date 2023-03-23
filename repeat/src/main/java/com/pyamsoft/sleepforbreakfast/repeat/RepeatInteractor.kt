package com.pyamsoft.sleepforbreakfast.repeat

import com.pyamsoft.sleepforbreakfast.db.repeat.DbRepeat
import com.pyamsoft.sleepforbreakfast.db.repeat.RepeatChangeEvent
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractor

internal interface RepeatInteractor : ListInteractor<DbRepeat.Id, DbRepeat, RepeatChangeEvent>
