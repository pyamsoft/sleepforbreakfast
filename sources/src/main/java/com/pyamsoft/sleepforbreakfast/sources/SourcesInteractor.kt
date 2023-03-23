package com.pyamsoft.sleepforbreakfast.sources

import com.pyamsoft.sleepforbreakfast.db.source.DbSource
import com.pyamsoft.sleepforbreakfast.db.source.SourceChangeEvent
import com.pyamsoft.sleepforbreakfast.money.list.ListInteractor

internal interface SourcesInteractor : ListInteractor<DbSource.Id, DbSource, SourceChangeEvent>
