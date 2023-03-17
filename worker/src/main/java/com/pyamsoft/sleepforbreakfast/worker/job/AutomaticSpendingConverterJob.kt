package com.pyamsoft.sleepforbreakfast.worker.job

import com.pyamsoft.sleepforbreakfast.worker.WorkJob

object AutomaticSpendingConverterJob : WorkJob {

  override val type = WorkJob.Type.AUTOMATIC_SPENDING_CONVERTER
}
