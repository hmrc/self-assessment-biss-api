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

package v2.retrieveBiss

import api.controllers.EndpointLogContext
import api.models.domain.{BusinessId, Nino, TaxYear, TypeOfBusiness}
import api.models.errors._
import api.models.outcomes.ResponseWrapper
import api.services.ServiceSpec
import org.scalatest.concurrent.ScalaFutures.convertScalaFuture
import v2.retrieveBiss.def1.model.response.{Loss, Profit, Total}
import v2.retrieveBiss.model.request.Def1_RetrieveBISSRequestData
import v2.retrieveBiss.model.response.Def1_RetrieveBISSResponse

import scala.concurrent.Future

class RetrieveBISSServiceSpec extends ServiceSpec {

  // WLOG
  private val requestData =
    Def1_RetrieveBISSRequestData(Nino("AA123456A"), TypeOfBusiness.`foreign-property`, TaxYear.fromMtd("2019-20"), BusinessId("XAIS12345678910"))

  private val response = Def1_RetrieveBISSResponse(Total(income = 100.00, 120.00, None, None, None), Profit(0.00, 0.00), Loss(20.0, 0.0))

  implicit val loggingContext: EndpointLogContext = EndpointLogContext("controller", "endpoint")

  trait Test extends MockRetrieveBISSConnector {
    val service = new RetrieveBISSService(mockConnector)
  }

  "retrieveBiss" should {
    "return a valid response" when {
      "a valid response is received from the downstream service" in new Test {
        MockRetrieveBISSConnector
          .retrieveBiss(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.retrieveBiss(requestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "return the error response as per the spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the downstream service" in new Test {

          MockRetrieveBISSConnector
            .retrieveBiss(requestData) returns Future.successful(
            Left(ResponseWrapper(correlationId, DownstreamErrors.single(DownstreamErrorCode(downstreamErrorCode)))))

          service.retrieveBiss(requestData).futureValue shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val errors = Seq(
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

      val extraTysErrors = Seq(
        "INVALID_TAX_YEAR"          -> TaxYearFormatError,
        "INVALID_INCOMESOURCE_ID"   -> BusinessIdFormatError,
        "INVALID_CORRELATION_ID"    -> InternalError,
        "INVALID_TAXABLE_ENTITY_ID" -> NinoFormatError,
        "INVALID_INCOME_SOURCETYPE" -> InternalError,
        "TAX_YEAR_NOT_SUPPORTED"    -> RuleTaxYearNotSupportedError
      )

      (errors ++ extraTysErrors).foreach(args => (serviceError _).tupled(args))
    }
  }

}
