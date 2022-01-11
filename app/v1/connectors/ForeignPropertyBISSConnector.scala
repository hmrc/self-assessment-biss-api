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

package v1.connectors

import config.AppConfig
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.HttpClient
import v1.connectors.httpparsers.StandardDesHttpParser._
import v1.models.requestData.RetrieveForeignPropertyBISSRequest
import v1.models.response.RetrieveForeignPropertyBISSResponse

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ForeignPropertyBISSConnector @Inject()(val http: HttpClient,
                                             val appConfig: AppConfig) extends BaseDesConnector {

  def retrieveBiss(request: RetrieveForeignPropertyBISSRequest)(
    implicit ec: ExecutionContext,
    hc: HeaderCarrier,
    correlationId: String): Future[DesOutcome[RetrieveForeignPropertyBISSResponse]] = {

    val nino = request.nino.nino
    val businessId = request.businessId
    val typeOfBusiness = request.typeOfBusiness.toString
    val taxYear = request.taxYear.toString

    get(
      DesUri[RetrieveForeignPropertyBISSResponse](s"income-tax/income-sources/nino/$nino/$typeOfBusiness/$taxYear/biss?incomesourceid=$businessId")
    )

  }
}