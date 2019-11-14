/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package utils

import java.time.{LocalDate, Year}

import v1.models.requestData.DesTaxYear

object DateUtils {

  def getDesTaxYear(taxYear: String): DesTaxYear = DesTaxYear.fromMtd(taxYear)

  def getDesTaxYear(current: LocalDate): DesTaxYear = {
    val fiscalYearStartDate = LocalDate.parse(s"${Year.now().toString}-04-05")

    if(current.isAfter(fiscalYearStartDate)) DesTaxYear(Year.now().getValue.+(1).toString)
      else DesTaxYear(Year.now().getValue.toString)
  }
}