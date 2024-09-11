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

import api.connectors.DownstreamUri.{IfsUri, TaxYearSpecificIfsUri}
import api.connectors.httpparsers.StandardDownstreamHttpParser._
import api.connectors.{BaseDownstreamConnector, DownstreamOutcome}
import config.AppConfig
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import v3.retrieveBiss.model.request.{Def1_RetrieveBISSRequestData, RetrieveBISSRequestData}
import v3.retrieveBiss.model.response.{Def1_RetrieveBISSResponse, RetrieveBISSResponse}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBISSConnector @Inject() (val http: HttpClient, val appConfig: AppConfig)(implicit ec: ExecutionContext)
    extends BaseDownstreamConnector {

  def retrieveBiss(
      request: RetrieveBISSRequestData)(implicit hc: HeaderCarrier, correlationId: String): Future[DownstreamOutcome[RetrieveBISSResponse]] = {

    import request._
    val incomeSourceType = typeOfBusiness.toIncomeSourceType

    request match {
      case def1: Def1_RetrieveBISSRequestData =>
        import def1._
        val (downstreamUri, queryParam) =
          if (taxYear.useTaxYearSpecificApi) {
            (
              TaxYearSpecificIfsUri[Def1_RetrieveBISSResponse](
                s"income-tax/income-sources/${taxYear.asTysDownstream}/$nino/$businessId/$incomeSourceType/biss"),
              Nil)
          } else {
            (
              IfsUri[Def1_RetrieveBISSResponse](s"income-tax/income-sources/nino/$nino/$incomeSourceType/${taxYear.asDownstream}/biss"),
              List("incomeSourceId" -> s"$businessId")
            )
          }

        val response = get(downstreamUri, queryParam)
        response
    }

  }

}
