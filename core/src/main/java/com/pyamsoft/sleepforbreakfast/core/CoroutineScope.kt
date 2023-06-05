package com.pyamsoft.sleepforbreakfast.core

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelChildren

fun CoroutineScope.cancelChildren() {
  this.coroutineContext[Job]?.cancelChildren()
}
