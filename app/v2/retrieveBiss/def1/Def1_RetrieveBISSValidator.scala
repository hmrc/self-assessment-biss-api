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

package v2.retrieveBiss.def1

import api.controllers.validators.Validator
import api.controllers.validators.resolvers.{ResolveBusinessId, ResolveNino, ResolveTaxYearMaximum, ResolveTypeOfBusiness}
import api.models.domain.TaxYear
import api.models.domain.TypeOfBusiness._
import api.models.errors.{MtdError, RuleTaxYearNotSupportedError}
import cats.data.Validated
import cats.data.Validated._
import cats.implicits._
import v2.retrieveBiss.model.request.{Def1_RetrieveBISSRequestData, RetrieveBISSRequestData}

import javax.inject.Singleton

@Singleton
class Def1_RetrieveBISSValidator(nino: String, typeOfBusiness: String, taxYear: String, businessId: String)
    extends Validator[RetrieveBISSRequestData] {

  private val resolveTaxYear = ResolveTaxYearMaximum(TaxYear.ending(2025))

  private val foreignPropertyMinimumTaxYear = TaxYear.fromMtd("2019-20")

  def validate: Validated[Seq[MtdError], RetrieveBISSRequestData] =
    (
      ResolveNino(nino),
      ResolveTypeOfBusiness(typeOfBusiness),
      resolveTaxYear(taxYear),
      ResolveBusinessId(businessId)
    ).mapN(Def1_RetrieveBISSRequestData) andThen validateTaxYear

  private def validateTaxYear(parsed: RetrieveBISSRequestData): Validated[Seq[MtdError], RetrieveBISSRequestData] = {
    val minTaxYear = parsed.typeOfBusiness match {
      case `foreign-property-fhl-eea` | `foreign-property`               => foreignPropertyMinimumTaxYear
      case `uk-property-non-fhl` | `uk-property-fhl` | `self-employment` => TaxYear.minimumTaxYear
    }

    if (parsed.taxYear.year < minTaxYear.year)
      Invalid(List(RuleTaxYearNotSupportedError))
    else
      Valid(parsed)
  }

}
