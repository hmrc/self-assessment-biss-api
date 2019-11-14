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

package v1.services

import support.UnitSpec
import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockSelfEmploymentBISSConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.{DesTaxYear, RetrieveSelfEmploymentBISSRequest}
import v1.models.response.selfEmployment.{Loss, Profit, RetrieveSelfEmploymentBISSResponse, Total}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SelfEmploymentBISSServiceSpec extends UnitSpec {

  private val nino = "AA123456A"
  private val taxYear = "2019"
  private val selfEmploymentId = "123456789"
  private val correlationId = "X-123"

  private val requestData = RetrieveSelfEmploymentBISSRequest(Nino(nino), DesTaxYear(taxYear), selfEmploymentId)

  trait Test extends MockSelfEmploymentBISSConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "retrieveSelfEmploymentBISS")

    val service = new SelfEmploymentBISSService(mockConnector)
  }

  val response =
    RetrieveSelfEmploymentBISSResponse (
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

  "retrieveBiss" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        MockSelfEmploymentBISSConnector.retrieveBiss(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))

        await(service.retrieveBiss(requestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "return error response" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockSelfEmploymentBISSConnector.retrieveBiss(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.retrieveBiss(requestData)) shouldBe Left(ErrorWrapper(Some(correlationId), error))
        }

      val input = Seq(

        ("INVALID_IDVALUE", NinoFormatError),
        ("INVALID_TAXYEAR", TaxYearFormatError),
        ("INVALID_INCOMESOURCEID", SelfEmploymentIdFormatError),
        ("NOT_FOUND", NotFoundError),
        ("INVALID_IDTYPE", DownstreamError),
        ("INVALID_INCOMESOURCETYPE", DownstreamError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}
