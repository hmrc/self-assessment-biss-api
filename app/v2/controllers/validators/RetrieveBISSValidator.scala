/*
 * Copyright 2023 HM Revenue & Customs
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

package v2.controllers.validators

import api.controllers.validators.RulesValidator
import api.models.domain.TypeOfBusiness
import api.models.errors.{MtdError, RuleTaxYearNotSupportedError}
import cats.data.Validated
import cats.data.Validated.Invalid
import config.FixedConfig
import v2.models.requestData.RetrieveBISSRequestData

object RetrieveBISSValidator extends RulesValidator[RetrieveBISSRequestData] with FixedConfig {

  override def validateBusinessRules(parsed: RetrieveBISSRequestData): Validated[Seq[MtdError], RetrieveBISSRequestData] = {
    val minTaxYear: Int = parsed.typeOfBusiness match {
      case TypeOfBusiness.`foreign-property-fhl-eea` | TypeOfBusiness.`foreign-property`                              => foreignPropertyMinTaxYear
      case TypeOfBusiness.`uk-property-non-fhl` | TypeOfBusiness.`uk-property-fhl` | TypeOfBusiness.`self-employment` => minimumTaxYear
    }

    val validatedTaxYear = if (parsed.taxYear.year < minTaxYear) Invalid(List(RuleTaxYearNotSupportedError)) else valid

    validatedTaxYear.onSuccess(parsed)
  }

}
