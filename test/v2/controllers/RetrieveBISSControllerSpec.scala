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

package v2.controllers

import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import uk.gov.hmrc.http.HeaderCarrier
import v2.mocks.MockIdGenerator
import v2.mocks.requestParsers.MockRetrieveBISSRequestDataParser
import v2.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockRetrieveBISSService}
import v2.models.domain.{Nino, TypeOfBusiness}
import v2.models.errors._
import v2.models.outcomes.ResponseWrapper
import v2.models.requestData.{RetrieveBISSRawData, RetrieveBISSRequest, TaxYear}
import v2.models.response.RetrieveBISSResponse
import v2.models.response.common.Total

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

class RetrieveBISSControllerSpec
    extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveBISSRequestDataParser
    with MockRetrieveBISSService
    with MockIdGenerator {

  private val nino           = "AA123456A"
  private val taxYear        = "2018-19"
  private val typeOfBusiness = "uk-property-fhl"
  private val correlationId  = "X-123"
  private val businessId     = "someBusinessId"

  // WLOG
  val response: RetrieveBISSResponse = RetrieveBISSResponse(Total(income = 100.00, None, None, None, None), None, None)

  val responseJson: JsValue = Json.parse("""{
                                                    |  "total": {
                                                    |    "income": 100.00
                                                    |  }
                                                    |}""".stripMargin)

  private val rawData     = RetrieveBISSRawData(nino, typeOfBusiness, taxYear, businessId)
  private val requestData = RetrieveBISSRequest(Nino(nino), TypeOfBusiness.`uk-property-fhl`, TaxYear.fromMtd(taxYear), businessId)

  trait Test {
    val hc: HeaderCarrier = HeaderCarrier()

    val controller = new RetrieveBISSController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestParser,
      retrieveBISSService = mockService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
    MockIdGenerator.generateCorrelationId.returns(correlationId)
  }

  "retrieveBiss" should {
    "return successful response with status OK" when {
      "valid request" in new Test {
        MockRetrieveBISSRequestDataParser.parse(rawData) returns Right(requestData)

        MockRetrieveBISSService
          .retrieveBiss(requestData) returns Future.successful(Right(ResponseWrapper(correlationId, response)))

        val result: Future[Result] =
          controller.retrieveBiss(nino = nino, typeOfBusiness = typeOfBusiness, taxYear = taxYear, businessId = businessId)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe responseJson
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {
            MockRetrieveBISSRequestDataParser.parse(rawData) returns Left(ErrorWrapper(correlationId, error, None))

            val result: Future[Result] =
              controller.retrieveBiss(nino = nino, typeOfBusiness = typeOfBusiness, taxYear = taxYear, businessId = businessId)(fakeGetRequest)

            contentAsJson(result) shouldBe Json.toJson(error)
            status(result) shouldBe expectedStatus
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (RuleTaxYearRangeInvalidError, BAD_REQUEST),
          (RuleTaxYearNotSupportedError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (TypeOfBusinessFormatError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {
            MockRetrieveBISSRequestDataParser.parse(rawData) returns Right(requestData)

            MockRetrieveBISSService.retrieveBiss(requestData) returns Future.successful(Left(ErrorWrapper(correlationId, mtdError)))

            val result: Future[Result] =
              controller.retrieveBiss(nino = nino, typeOfBusiness = typeOfBusiness, taxYear = taxYear, businessId = businessId)(fakeGetRequest)

            contentAsJson(result) shouldBe Json.toJson(mtdError)
            status(result) shouldBe expectedStatus
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (TypeOfBusinessFormatError, BAD_REQUEST),
          (BusinessIdFormatError, BAD_REQUEST),
          (RuleNoIncomeSubmissionsExist, FORBIDDEN),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }

}
