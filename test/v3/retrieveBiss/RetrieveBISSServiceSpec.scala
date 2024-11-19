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

package v3.retrieveBiss

import api.controllers.EndpointLogContext
import api.models.domain.{BusinessId, Nino, TaxYear}
import v3.retrieveBiss.model.domain.TypeOfBusiness
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import v3.retrieveBiss.def1.model.response.{Loss, Profit, Total}
import v3.retrieveBiss.model.request.Def1_RetrieveBISSRequestData
import v3.retrieveBiss.model.response.Def1_RetrieveBISSResponse

import scala.concurrent.Future

class RetrieveBISSServiceSpec extends ServiceSpec {

  // WLOG
  private def requestData(taxYear: String): Def1_RetrieveBISSRequestData = Def1_RetrieveBISSRequestData(
    nino = Nino("AA123456A"),
    typeOfBusiness = TypeOfBusiness.`foreign-property`,
    taxYear = TaxYear.fromMtd(taxYear),
    businessId = BusinessId("XAIS12345678910")
  )

  private val response: Def1_RetrieveBISSResponse = Def1_RetrieveBISSResponse(
    total = Total(income = 100.00, 120.00, None, None, None),
    profit = Profit(0.00, 0.00),
    loss = Loss(20.0, 0.0)
  )

  implicit val loggingContext: EndpointLogContext = EndpointLogContext("controller", "endpoint")

  private trait Test extends MockRetrieveBISSConnector {
    val service: RetrieveBISSService = new RetrieveBISSService(mockConnector)
  }

  "RetrieveBISSService" when {
    "retrieveBiss" should {
      "return a valid response" when {
        "a valid response is received from the downstream service" in new Test {
          MockRetrieveBISSConnector
            .retrieveBiss(requestData("2025-26"))
            .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

          await(service.retrieveBiss(requestData("2025-26"))) shouldBe Right(ResponseWrapper(correlationId, response))
        }
      }

      "return error response as per the spec" when {
        def serviceError(taxYear: String, downstreamErrorMapping: Seq[(String, MtdError)]): Unit = {
          val fullDownstreamErrorMap: Seq[(String, MtdError)] = downstreamErrorMapping ++ Seq(
            "UNMATCHED_STUB_ERROR" -> RuleIncorrectGovTestScenarioError
          )
          fullDownstreamErrorMap.foreach { case (downstreamErrorCode, expectedError) =>
            s"the $downstreamErrorCode error for tax year $taxYear is returned from the downstream service" in new Test {
              MockRetrieveBISSConnector
                .retrieveBiss(requestData(taxYear))
                .returns(Future.successful(
                  Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode))))
                ))

              await(service.retrieveBiss(requestData(taxYear))) shouldBe Left(ErrorWrapper(correlationId, expectedError))
            }
          }
        }

        val api1415ErrorMap: Seq[(String, MtdError)] = Seq(
          "INVALID_IDVALUE"              -> NinoFormatError,
          "INVALID_TAXYEAR"              -> TaxYearFormatError,
          "INVALID_IDTYPE"               -> InternalError,
          "INVALID_CORRELATIONID"        -> InternalError,
          "INVALID_INCOMESOURCETYPE"     -> InternalError,
          "INVALID_INCOMESOURCEID"       -> BusinessIdFormatError,
          "INCOME_SUBMISSIONS_NOT_EXIST" -> RuleNoIncomeSubmissionsExist,
          "INVALID_ACCOUNTING_PERIOD"    -> InternalError,
          "INVALID_QUERY_PARAM"          -> InternalError,
          "NOT_FOUND"                    -> NotFoundError,
          "SERVER_ERROR"                 -> InternalError,
          "SERVICE_UNAVAILABLE"          -> InternalError
        )

        val api1871ErrorMap: Seq[(String, MtdError)] = Seq(
          "INVALID_TAXABLE_ENTITY_ID"    -> NinoFormatError,
          "INVALID_TAX_YEAR"             -> TaxYearFormatError,
          "INVALID_CORRELATION_ID"       -> InternalError,
          "INVALID_INCOMESOURCE_TYPE"    -> InternalError,
          "INVALID_INCOMESOURCE_ID"      -> BusinessIdFormatError,
          "INCOME_SUBMISSIONS_NOT_EXIST" -> RuleNoIncomeSubmissionsExist,
          "INVALID_ACCOUNTING_PERIOD"    -> InternalError,
          "INVALID_QUERY_PARAM"          -> InternalError,
          "TAX_YEAR_NOT_SUPPORTED"       -> RuleTaxYearNotSupportedError,
          "NOT_FOUND"                    -> NotFoundError,
          "SERVER_ERROR"                 -> InternalError,
          "SERVICE_UNAVAILABLE"          -> InternalError
        )

        val api1879ErrorMap: Seq[(String, MtdError)] = Seq(
          "INVALID_TAXABLE_ENTITY_ID"        -> NinoFormatError,
          "INVALID_TAX_YEAR"                 -> TaxYearFormatError,
          "INVALID_CORRELATION_ID"           -> InternalError,
          "INVALID_INCOME_SOURCE_TYPE"       -> InternalError,
          "INVALID_INCOME_SOURCE_ID"         -> BusinessIdFormatError,
          "INCOME_SUBMISSIONS_NOT_EXIST"     -> RuleNoIncomeSubmissionsExist,
          "INVALID_ACCOUNTING_PERIOD"        -> InternalError,
          "INVALID_QUERY_PARAM"              -> InternalError,
          "TAX_YEAR_NOT_SUPPORTED"           -> RuleTaxYearNotSupportedError,
          "REQUESTED_TAX_YEAR_NOT_SUPPORTED" -> RuleTaxYearNotSupportedError,
          "NOT_FOUND"                        -> NotFoundError,
          "SERVER_ERROR"                     -> InternalError,
          "SERVICE_UNAVAILABLE"              -> InternalError
        )

        val inputs: Seq[(String, Seq[(String, MtdError)])] = Seq(
          ("2022-23", api1415ErrorMap),
          ("2023-24", api1871ErrorMap),
          ("2024-25", api1871ErrorMap),
          ("2025-26", api1879ErrorMap)
        )

        inputs.foreach(args => (serviceError _).tupled(args))
      }
    }
  }
}
