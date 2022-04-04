/*
 * Copyright 2022 HM Revenue & Customs
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

package v2.services

import v2.controllers.EndpointLogContext
import v2.mocks.connectors.MockRetrieveBISSConnector
import v2.models.domain.{Nino, TypeOfBusiness}
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.requestData.{TaxYear, RetrieveBISSRequest}
import v2.models.response.RetrieveBISSResponse
import v2.models.response.common.Total

import scala.concurrent.Future

class RetrieveBISSServiceSpec extends ServiceSpec {

  // WLOG
  private val requestData = RetrieveBISSRequest(Nino("AA123456A"), TypeOfBusiness.`foreign-property`, TaxYear.fromMtd("2019-20"), "XAIS12345678910")
  private val response    = RetrieveBISSResponse(Total(income = 100.00, None, None, None, None), None, None)

  private val correlationIdIn  = "correlation-id-in"
  private val correlationIdOut = "correlation-id-out"

  implicit val loggingContext: EndpointLogContext = EndpointLogContext("controller", "endpoint")

  trait Test extends MockRetrieveBISSConnector {
    val service = new RetrieveBISSService(mockConnector)
  }

  "retrieveBiss" should {
    "return a valid response" when {
      "a valid response is received from the downstream service" in new Test {
        MockRetrieveBISSConnector.retrieveBiss(requestData, correlationIdIn) returns Future.successful(
          Right(ResponseWrapper(correlationIdOut, response)))

        service.retrieveBiss(requestData, correlationIdIn).futureValue shouldBe Right(ResponseWrapper(correlationIdOut, response))
      }
    }

    "return the error response as per the spec" when {

      def serviceError(downstreamErrorCode: String, error: MtdError): Unit =
        s"a $downstreamErrorCode error is returned from the downstream service" in new Test {

          MockRetrieveBISSConnector.retrieveBiss(requestData, correlationIdIn) returns Future.successful(
            Left(ResponseWrapper(correlationIdOut, IfsErrors.single(IfsErrorCode(downstreamErrorCode)))))

          service.retrieveBiss(requestData, correlationIdIn).futureValue shouldBe Left(ErrorWrapper(correlationIdOut, error))
        }

      val input = Seq(
        "INVALID_IDVALUE"              -> NinoFormatError,
        "INVALID_TAXYEAR"              -> TaxYearFormatError,
        "INVALID_IDTYPE"               -> DownstreamError,
        "INVALID_CORRELATIONID"        -> DownstreamError,
        "INVALID_INCOMESOURCETYPE"     -> DownstreamError,
        "INVALID_INCOMESOURCEID"       -> BusinessIdFormatError,
        "INCOME_SUBMISSIONS_NOT_EXIST" -> RuleNoIncomeSubmissionsExist,
        "INVALID_ACCOUNTING_PERIOD"    -> DownstreamError,
        "INVALID_QUERY_PARAM"          -> DownstreamError,
        "NOT_FOUND"                    -> NotFoundError,
        "SERVER_ERROR"                 -> DownstreamError,
        "SERVICE_UNAVAILABLE"          -> DownstreamError
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }

}
