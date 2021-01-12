/*
 * Copyright 2021 HM Revenue & Customs
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

import uk.gov.hmrc.domain.Nino
import uk.gov.hmrc.http.HeaderCarrier
import v1.controllers.EndpointLogContext
import v1.mocks.connectors.MockForeignPropertyBISSConnector
import v1.models.des.IncomeSourceType
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.{DesTaxYear, RetrieveForeignPropertyBISSRequest}
import v1.models.response.RetrieveForeignPropertyBISSResponse
import v1.models.response.common.{Loss, Profit, Total}

import scala.concurrent.Future

class ForeignPropertyBISSServiceSpec extends ServiceSpec {

  private val nino = Nino("AA123456A")
  private val taxYear = "2019"
  private val businessId = "XAIS12345678910"

  private val requestData = RetrieveForeignPropertyBISSRequest(nino, businessId, IncomeSourceType.`foreign-property`,DesTaxYear(taxYear))

  trait Test extends MockForeignPropertyBISSConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "retrieveSelfEmploymentPropertyBISS")

    val service = new ForeignPropertyBISSService(mockConnector)
  }

  val response: RetrieveForeignPropertyBISSResponse = RetrieveForeignPropertyBISSResponse(
    Total(
      income = 100.00,
      expenses = Some(50.00),
      additions = Some(5.00),
      deductions = Some(60.00)
    ),
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
        MockForeignPropertyBISSConnector.retrieveBiss(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, response))))
        await(service.retrieveBiss(requestData)) shouldBe Right(ResponseWrapper(correlationId, response))
      }
    }

    "return error response" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockForeignPropertyBISSConnector.retrieveBiss(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.retrieveBiss(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
        }

      val input = Seq(
        ("INVALID_IDVALUE", NinoFormatError),
        ("INVALID_TAXYEAR", TaxYearFormatError),
        ("INVALID_INCOMESOURCEID", BusinessIdFormatError),
        ("NOT_FOUND", NotFoundError),
        ("INVALID_IDTYPE", DownstreamError),
        ("INVALID_INCOMESOURCETYPE", TypeOfBusinessFormatError),
        ("SERVER_ERROR", DownstreamError),
        ("SERVICE_UNAVAILABLE", DownstreamError)
      )

      input.foreach(args => (serviceError _).tupled(args))
    }
  }
}