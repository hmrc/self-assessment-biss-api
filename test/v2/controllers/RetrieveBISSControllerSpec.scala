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

package v2.controllers

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.{BusinessId, Nino, TaxYear, TypeOfBusiness}
import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v2.controllers.requestParsers.MockRetrieveBISSRequestDataDataParser
import v2.models.requestData.{RetrieveBISSRawData, RetrieveBISSRequestData}
import v2.models.response.RetrieveBISSResponse
import v2.models.response.common.{Loss, Profit, Total}
import v2.services.MockRetrieveBISSService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveBISSControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveBISSRequestDataDataParser
    with MockRetrieveBISSService {

  val response: RetrieveBISSResponse =
    RetrieveBISSResponse(Total(income = 100.00, expenses = 50.0, None, None, None), Profit(net = 0.0, taxable = 0.0), Loss(net = 50.0, taxable = 0.0))

  val responseJson: JsValue = Json.parse("""{
      |  "total": {
      |    "income": 100.00,
      |    "expenses": 50.00
      |  },
      |  "loss": {
      |    "net": 50.0,
      |    "taxable": 0.00
      |  },
      |  "profit": {
      |    "net": 0.00,
      |    "taxable": 0.00
      |  }
      |}""".stripMargin)

  private val taxYear        = "2018-19"
  private val typeOfBusiness = "uk-property-fhl"
  private val businessId     = "someBusinessId"
  private val rawData        = RetrieveBISSRawData(nino, typeOfBusiness, taxYear, businessId)
  private val requestData    = RetrieveBISSRequestData(Nino(nino), TypeOfBusiness.`uk-property-fhl`, TaxYear.fromMtd(taxYear), BusinessId(businessId))

  "retrieveBiss" should {
    "return successful response with status OK" when {
      "valid request" in new Test {

        MockRetrieveBISSRequestDataParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveBISSService
          .retrieveBiss(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {

        MockRetrieveBISSRequestDataParser
          .parse(rawData)
          .returns(Left(ErrorWrapper(correlationId, NinoFormatError, None)))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {

        MockRetrieveBISSRequestDataParser
          .parse(rawData)
          .returns(Right(requestData))

        MockRetrieveBISSService
          .retrieveBiss(requestData)
          .returns(Future.successful(Left(ErrorWrapper(correlationId, TaxYearFormatError))))

        runErrorTest(TaxYearFormatError)
      }
    }
  }

  trait Test extends ControllerTest {

    val controller = new RetrieveBISSController(
      authService = mockEnrolmentsAuthService,
      lookupService = mockMtdIdLookupService,
      requestParser = mockRequestParser,
      service = mockService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    protected def callController(): Future[Result] = {
      controller.retrieveBiss(nino, typeOfBusiness, taxYear, businessId)(fakeGetRequest)
    }

  }

}
