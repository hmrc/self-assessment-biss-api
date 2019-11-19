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

package v1.controllers

import fixtures.RetrieveUKPropertyFixture._
import play.api.libs.json.Json
import play.api.mvc.Result
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.mocks.requestParsers.MockRetrieveUKPropertyBISSRequestDataParser
import v1.mocks.services.{MockEnrolmentsAuthService, MockMtdIdLookupService, MockUKPropertyBISSService}
import v1.models.des.IncomeSourceType
import v1.models.errors.{BadRequestError, DownstreamError, ErrorWrapper, MtdError, NinoFormatError, NotFoundError, SelfEmploymentIdFormatError, TaxYearFormatError, TypeOfBusinessFormatError}
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.{DesTaxYear, RetrieveUKPropertyBISSRawData, RetrieveUKPropertyBISSRequest}
import v1.models.response.RetrieveUKPropertyBISSResponse
import v1.models.response.common.{Loss, Profit, Total}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveUKPropertyBISSControllerSpec
  extends ControllerBaseSpec
    with MockEnrolmentsAuthService
    with MockMtdIdLookupService
    with MockRetrieveUKPropertyBISSRequestDataParser
    with MockUKPropertyBISSService {

  trait Test {
  val hc = HeaderCarrier()

    val controller = new RetrieveUKPropertyBISSController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestParser,
      ukPropertyBISSService = mockService,
      cc =  cc
    )

    MockedMtdIdLookupService.lookup(nino).returns(Future.successful(Right("test-mtd-id")))
    MockedEnrolmentsAuthService.authoriseUser()
  }

  private val nino            = "AA123456A"
  private val taxYear         =  Some("2018-19")
  private val typeOfBusiness  = "uk-property-fhl"
  private val correlationId   = "X-123"

  val response =
    RetrieveUKPropertyBISSResponse (
      Total(
        income = 100.00,
        expenses = Some(50.00),
        additions = Some(5.00),
        deductions = Some(60.00)
      ),
      accountingAdjustments = Some(-30.00),
      Some(Profit(
        net = Some(20.00),
        taxable = Some(10.00)
      )),
      Some(Loss(
        net = Some(10.00),
        taxable = Some(35.00)
      ))
    )

  private val rawData     = RetrieveUKPropertyBISSRawData(nino, taxYear, typeOfBusiness)
  private val requestData = RetrieveUKPropertyBISSRequest(Nino(nino), DesTaxYear("2019"), IncomeSourceType.`uk-property`)

  "retrieveBiss" should {
    "return successful response with status OK" when {
      "happy path" in new Test {

        MockRetrieveUKPropertyBISSRequestDataParser
          .parse(rawData)
          .returns(Right(requestData))

        MockUKPropertyBISSService
          .retrieveBiss(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseObj))))

        val result: Future[Result] = controller.retrieveBiss(nino, taxYear, typeOfBusiness)(fakeGetRequest)

        status(result) shouldBe OK
        contentAsJson(result) shouldBe mtdResponse
        header("X-CorrelationId", result) shouldBe Some(correlationId)
      }
    }

    "return the error as per spec" when {
      "parser errors occur" must {
        def errorsFromParserTester(error: MtdError, expectedStatus: Int): Unit = {
          s"a ${error.code} error is returned from the parser" in new Test {

            MockRetrieveUKPropertyBISSRequestDataParser
              .parse(rawData)
              .returns(Left(ErrorWrapper(Some(correlationId), error, None)))

            val result: Future[Result] = controller.retrieveBiss(nino, taxYear,  typeOfBusiness)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(error)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (BadRequestError, BAD_REQUEST),
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (TypeOfBusinessFormatError, BAD_REQUEST),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (errorsFromParserTester _).tupled(args))
      }

      "service errors occur" must {
        def serviceErrors(mtdError: MtdError, expectedStatus: Int): Unit = {
          s"a $mtdError error is returned from the service" in new Test {

            MockRetrieveUKPropertyBISSRequestDataParser
              .parse(rawData)
              .returns(Right(requestData))

            MockUKPropertyBISSService
              .retrieveBiss(requestData)
              .returns(Future.successful(Left(ErrorWrapper(Some(correlationId), mtdError))))

            val result: Future[Result] = controller.retrieveBiss(nino, taxYear, typeOfBusiness)(fakeGetRequest)

            status(result) shouldBe expectedStatus
            contentAsJson(result) shouldBe Json.toJson(mtdError)
            header("X-CorrelationId", result) shouldBe Some(correlationId)
          }
        }

        val input = Seq(
          (NinoFormatError, BAD_REQUEST),
          (TaxYearFormatError, BAD_REQUEST),
          (TypeOfBusinessFormatError, BAD_REQUEST),
          (NotFoundError, NOT_FOUND),
          (DownstreamError, INTERNAL_SERVER_ERROR)
        )

        input.foreach(args => (serviceErrors _).tupled(args))
      }
    }
  }
}
