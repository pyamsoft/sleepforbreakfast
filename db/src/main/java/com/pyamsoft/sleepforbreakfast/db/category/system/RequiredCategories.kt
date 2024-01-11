/*
 * Copyright 2024 pyamsoft
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pyamsoft.sleepforbreakfast.db.category.system

enum class RequiredCategories(val displayName: String) {
  // Food
  GROCERIES("Groceries"),
  RESTAURANTS("Restaurants"),
  FAST_FOOD("Fast Food"),
  DELIVERY("Delivery"),

  // Entertainment
  MOVIES("Movies"),
  VIDEO_GAMES("Video Games"),
  STREAMING("Streaming"),
  ONLINE_SHOPPING("Online Shopping"),
  ENTERTAINMENT("Entertainment"),

  // Utility
  WATER_BILL("Water Bill"),
  GAS_BILL("Gas Bill"),
  ELECTRICITY_BILL("Electricity Bill"),
  INTERNET_BILL("Internet Bill"),

  // Work
  RENT("Rent"),
  MORTGAGE("Mortgage"),
  SALARY("Salary"),
}
