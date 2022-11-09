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

package v2.connectors

import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v2.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import v2.connectors.httpparsers.StandardDownstreamHttpParser._
import v2.models.requestData.RetrieveBISSRequest
import v2.models.response.RetrieveBISSResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBISSConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends BaseDownstreamConnector {

  def retrieveBiss(
      request: RetrieveBISSRequest)(implicit hc: HeaderCarrier, correlationId: String): Future[DownstreamOutcome[RetrieveBISSResponse]] = {

    import request._
    val incomeSourceType = typeOfBusiness.toIncomeSourceType

    if (taxYear.useTaxYearSpecificApi) {
      get(
        uri = TaxYearSpecificIfsUri[RetrieveBISSResponse](
          s"individuals/self-assessment/income-summary/${nino.nino}/$incomeSourceType/${taxYear.asTysDownstream}/$businessId"
        )
      )
    } else {
      get(
        uri = IfsUri[RetrieveBISSResponse](s"income-tax/income-sources/nino/${nino.nino}/$incomeSourceType/${taxYear.asDownstream}/biss"),
        queryParams = Seq("incomeSourceId" -> businessId)
      )
    }

  }

}
