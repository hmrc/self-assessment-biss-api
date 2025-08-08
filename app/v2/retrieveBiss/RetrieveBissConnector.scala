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

package v2.retrieveBiss

import api.connectors.DownstreamUri.{HipUri, IfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser.*
import api.connectors.*
import config.{AppConfig, ConfigFeatureSwitches}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import v2.retrieveBiss.model.request.{Def1_RetrieveBissRequestData, RetrieveBissRequestData}
import v2.retrieveBiss.model.response.{Def1_RetrieveBissResponse, RetrieveBissResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBissConnector @Inject() (val http: HttpClientV2, val appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends BaseDownstreamConnector {

  def retrieveBiss(
      request: RetrieveBissRequestData)(implicit hc: HeaderCarrier, correlationId: String): Future[DownstreamOutcome[RetrieveBissResponse]] = {

    import request.*
    val incomeSourceType = typeOfBusiness.toIncomeSourceType

    lazy val downstreamUri1871: DownstreamUri[Def1_RetrieveBissResponse] =
      if (ConfigFeatureSwitches().isEnabled("ifs_hip_migration_1871")) {
        HipUri(s"itsa/income-tax/v1/income-sources/${taxYear.asTysDownstream}/$nino/$businessId/$incomeSourceType/biss")
      } else {
        IfsUri(s"income-tax/income-sources/${taxYear.asTysDownstream}/$nino/$businessId/$incomeSourceType/biss")
      }

    lazy val downstreamUri1415: DownstreamUri[Def1_RetrieveBissResponse] =
      IfsUri(s"income-tax/income-sources/nino/$nino/$incomeSourceType/${taxYear.asDownstream}/biss")

    request match {
      case def1: Def1_RetrieveBissRequestData =>
        import def1.*
        val (downstreamUri, queryParam) =
          if (taxYear.useTaxYearSpecificApi) {
            (downstreamUri1871, Nil)
          } else {
            (downstreamUri1415, List("incomeSourceId" -> s"$businessId"))
          }

        val response = get(downstreamUri, queryParam)
        response
    }

  }

}
