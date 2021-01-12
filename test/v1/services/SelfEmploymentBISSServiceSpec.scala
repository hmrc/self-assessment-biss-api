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
import v1.mocks.connectors.MockSelfEmploymentBISSConnector
import v1.models.errors._
import v1.models.outcomes.ResponseWrapper
import v1.models.requestData.{DesTaxYear, RetrieveSelfEmploymentBISSRequest}
import fixtures.RetrieveSelfEmploymentBISSFixture._

import scala.concurrent.Future

class SelfEmploymentBISSServiceSpec extends ServiceSpec {

  private val nino = "AA123456A"
  private val taxYear = "2019"
  private val selfEmploymentId = "123456789"

  private val requestData = RetrieveSelfEmploymentBISSRequest(Nino(nino), DesTaxYear(taxYear), selfEmploymentId)

  trait Test extends MockSelfEmploymentBISSConnector {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val logContext: EndpointLogContext = EndpointLogContext("controller", "retrieveSelfEmploymentBISS")

    val service = new SelfEmploymentBISSService(mockConnector)
  }

  "retrieveBiss" should {
    "return a valid response" when {
      "a valid request is supplied" in new Test {
        MockSelfEmploymentBISSConnector.retrieveBiss(requestData)
          .returns(Future.successful(Right(ResponseWrapper(correlationId, responseObj))))

        await(service.retrieveBiss(requestData)) shouldBe Right(ResponseWrapper(correlationId, responseObj))
      }
    }

    "return error response" when {

      def serviceError(desErrorCode: String, error: MtdError): Unit =
        s"a $desErrorCode error is returned from the service" in new Test {

          MockSelfEmploymentBISSConnector.retrieveBiss(requestData)
            .returns(Future.successful(Left(ResponseWrapper(correlationId, DesErrors.single(DesErrorCode(desErrorCode))))))

          await(service.retrieveBiss(requestData)) shouldBe Left(ErrorWrapper(correlationId, error))
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