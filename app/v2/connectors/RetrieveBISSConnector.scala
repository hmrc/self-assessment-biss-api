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

package v2.connectors

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.models.requestData.RetrieveBISSRequestData
import v2.models.response.RetrieveBISSResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBISSConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends BaseDownstreamConnector {

  def retrieveBiss(
      request: RetrieveBISSRequestData)(implicit hc: HeaderCarrier, correlationId: String): Future[DownstreamOutcome[RetrieveBISSResponse]] = {

    import request._
    val incomeSourceType = typeOfBusiness.toIncomeSourceType

    if (taxYear.useTaxYearSpecificApi) {
      get(
        uri = TaxYearSpecificIfsUri[RetrieveBISSResponse](
          s"income-tax/income-sources/${taxYear.asTysDownstream}/$nino/$businessId/$incomeSourceType/biss"
        )
      )
    } else {
      get(
        uri = IfsUri[RetrieveBISSResponse](s"income-tax/income-sources/nino/$nino/$incomeSourceType/${taxYear.asDownstream}/biss"),
        queryParams = Seq("incomeSourceId" -> s"$businessId")
      )
    }

  }

}
