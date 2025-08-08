/*
 * Copyright 2025 HM Revenue & Customs
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

import api.connectors.DownstreamOutcome
import org.scalamock.handlers.CallHandler
import org.scalamock.scalatest.MockFactory
import org.scalatest.TestSuite
import uk.gov.hmrc.http.HeaderCarrier
import v3.retrieveBiss.model.request.RetrieveBissRequestData
import v3.retrieveBiss.model.response.RetrieveBissResponse

import scala.concurrent.Future

trait MockRetrieveBissConnector extends TestSuite with MockFactory {

  val mockConnector: RetrieveBissConnector = mock[RetrieveBissConnector]

  object MockRetrieveBissConnector {

    def retrieveBiss(requestData: RetrieveBissRequestData): CallHandler[Future[DownstreamOutcome[RetrieveBissResponse]]] = {
      (mockConnector
        .retrieveBiss(_: RetrieveBissRequestData)(_: HeaderCarrier, _: String))
        .expects(requestData, *, *)
    }

  }

}
