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

import api.controllers.{ControllerBaseSpec, ControllerTestRunner}
import api.models.domain.{BusinessId, Nino, TaxYear, TypeOfBusiness}
import api.models.errors.{ErrorWrapper, NinoFormatError, TaxYearFormatError}
import api.models.outcomes.ResponseWrapper
import config.MockAppConfig
import play.api.Configuration
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import v3.retrieveBiss.def1.model.response.{Loss, Profit, Total}
import v3.retrieveBiss.model.request.Def1_RetrieveBISSRequestData
import v3.retrieveBiss.model.response.{Def1_RetrieveBISSResponse, RetrieveBISSResponse}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class RetrieveBISSControllerSpec
    extends ControllerBaseSpec
    with ControllerTestRunner
    with MockRetrieveBISSValidatorFactory
    with MockRetrieveBISSService
    with MockAppConfig {

  val response: RetrieveBISSResponse =
    Def1_RetrieveBISSResponse(
      Total(income = 100.00, expenses = 50.0, None, None, None),
      Profit(net = 0.0, taxable = 0.0),
      Loss(net = 50.0, taxable = 0.0))

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

  private val requestData =
    Def1_RetrieveBISSRequestData(Nino(nino), TypeOfBusiness.`uk-property-fhl`, TaxYear.fromMtd(taxYear), BusinessId(businessId))

  "retrieveBiss" should {
    "return successful response with status OK" when {
      "valid request" in new Test {
        willUseValidator(returningSuccess(requestData))

        MockRetrieveBISSService
          .retrieveBiss(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        runOkTest(expectedStatus = OK, maybeExpectedResponseBody = Some(responseJson))
      }
    }

    "return the error as per spec" when {
      "the parser validation fails" in new Test {
        willUseValidator(returning(NinoFormatError))

        runErrorTest(NinoFormatError)
      }

      "the service returns an error" in new Test {
        willUseValidator(returningSuccess(requestData))

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
      validatorFactory = mockRetrieveBISSValidatorFactory,
      service = mockService,
      cc = cc,
      idGenerator = mockIdGenerator
    )

    MockedAppConfig.featureSwitches.anyNumberOfTimes() returns Configuration(
      "supporting-agents-access-control.enabled" -> true
    )

    MockedAppConfig.endpointAllowsSupportingAgents(controller.endpointName).anyNumberOfTimes() returns false

    protected def callController(): Future[Result] = {
      controller.retrieveBiss(nino, typeOfBusiness, taxYear, businessId)(fakeGetRequest)
    }

  }

}
