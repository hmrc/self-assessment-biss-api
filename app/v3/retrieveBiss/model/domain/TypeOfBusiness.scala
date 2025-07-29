/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.retrieveBiss.model.domain

import api.models.des.IncomeSourceType
import api.models.domain.TaxYear
import play.api.libs.json.Format
import utils.enums.Enums

sealed trait TypeOfBusiness {
  def toIncomeSourceType(taxYear: Int): IncomeSourceType
}

object TypeOfBusiness {

  val fhlPropertyMinimumTaxYear: TaxYear = TaxYear.fromMtd("2025-26")

  private def incomeSourceTypeFor(preThresholdType: IncomeSourceType, postThresholdType: Option[IncomeSourceType], taxYear: Int): IncomeSourceType =
    if (taxYear >= fhlPropertyMinimumTaxYear.year) {
      postThresholdType.getOrElse(
        throw new IllegalArgumentException(
          s"Unsupported income source type: $preThresholdType for tax year: $taxYear"
        )
      )
    } else {
      preThresholdType
    }

  case object `uk-property` extends TypeOfBusiness {

    override def toIncomeSourceType(taxYear: Int): IncomeSourceType =
      incomeSourceTypeFor(
        preThresholdType = IncomeSourceType.`uk-property`,
        postThresholdType = Some(IncomeSourceType.`02`),
        taxYear = taxYear
      )

  }

  case object `uk-property-fhl` extends TypeOfBusiness {

    override def toIncomeSourceType(taxYear: Int): IncomeSourceType =
      incomeSourceTypeFor(
        preThresholdType = IncomeSourceType.`fhl-property-uk`,
        postThresholdType = None,
        taxYear = taxYear
      )

  }

  case object `foreign-property` extends TypeOfBusiness {

    override def toIncomeSourceType(taxYear: Int): IncomeSourceType =
      incomeSourceTypeFor(
        preThresholdType = IncomeSourceType.`foreign-property`,
        postThresholdType = Some(IncomeSourceType.`15`),
        taxYear = taxYear
      )

  }

  case object `foreign-property-fhl-eea` extends TypeOfBusiness {

    override def toIncomeSourceType(taxYear: Int): IncomeSourceType =
      incomeSourceTypeFor(
        preThresholdType = IncomeSourceType.`fhl-property-eea`,
        postThresholdType = None,
        taxYear = taxYear
      )

  }

  case object `self-employment` extends TypeOfBusiness {

    override def toIncomeSourceType(taxYear: Int): IncomeSourceType =
      incomeSourceTypeFor(
        preThresholdType = IncomeSourceType.`self-employment`,
        postThresholdType = Some(IncomeSourceType.`01`),
        taxYear = taxYear
      )

  }

  val values: Array[TypeOfBusiness] = Array(
    `uk-property`,
    `uk-property-fhl`,
    `foreign-property`,
    `foreign-property-fhl-eea`,
    `self-employment`
  )

  val parser: PartialFunction[String, TypeOfBusiness] = Enums.parser(values)
  implicit val format: Format[TypeOfBusiness]         = Enums.format(values)
}
