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

import support.UnitSpec
import v1.models.requestData.DesTaxYear

class DateUtilsSpec extends UnitSpec{

  "getDesTaxYear" should {
    "return a valid DesTaxYear" when {
      "mtd formatted string tax year is supplied" in {
        DateUtils.getDesTaxYear(Some("2018-19")) shouldBe DesTaxYear("2019")
      }

      "no tax year is supplied in the first quarter" in {
        DateUtils.getDesTaxYear(None) shouldBe DesTaxYear(Year.now().getValue.toString)
      }
    }
  }

  "getDesTaxYear with date param" should {
    "return a valid DesTaxYear" when {
      "mtd formatted string tax year is supplied" in {
        DateUtils.getDesTaxYear(Some("2018-19"), LocalDate.now()) shouldBe DesTaxYear("2019")
      }

      "no tax year is supplied when the date is 5th April of the current year" in {
        DateUtils.getDesTaxYear(None, LocalDate.parse(s"${Year.now().toString}-04-05")) shouldBe DesTaxYear(Year.now().getValue.toString)
      }

      "no tax year is supplied when the date is 6th April of the current year" in {
        DateUtils.getDesTaxYear(None, LocalDate.parse(s"${Year.now().toString}-04-06")) shouldBe DesTaxYear(Year.now().getValue.+(1).toString)
      }
    }
  }
}
