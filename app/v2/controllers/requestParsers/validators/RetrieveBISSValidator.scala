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

package v2.controllers.requestParsers.validators

import api.controllers.requestParsers.validators.validations.{BusinessIdValidation, NinoValidation, TaxYearValidation, TypeOfBusinessValidation}
import api.models.errors.MtdError
import config.FixedConfig
import v2.models.requestData.RetrieveBISSRawData

class RetrieveBISSValidator extends Validator[RetrieveBISSRawData] with FixedConfig {

  private val validationSet = List(parameterFormatValidation)

  private def parameterFormatValidation: RetrieveBISSRawData => List[List[MtdError]] = (data: RetrieveBISSRawData) =>
    List(
      NinoValidation.validate(data.nino),
      TypeOfBusinessValidation.validate(data.typeOfBusiness),
      data.typeOfBusiness match {
        case "foreign-property-fhl-eea" | "foreign-property"               => TaxYearValidation.validate(foreignPropertyMinTaxYear, data.taxYear)
        case "uk-property-non-fhl" | "uk-property-fhl" | "self-employment" => TaxYearValidation.validate(minimumTaxYear, data.taxYear)
        case _                                                             => Nil
      },
      BusinessIdValidation.validate(data.businessId)
    )

  override def validate(data: RetrieveBISSRawData): List[MtdError] = run(validationSet, data).distinct
}
