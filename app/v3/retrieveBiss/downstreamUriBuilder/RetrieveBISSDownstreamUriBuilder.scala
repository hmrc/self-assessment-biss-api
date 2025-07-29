/*
 * Copyright 2024 HM Revenue & Customs
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

package v3.retrieveBiss.downstreamUriBuilder

import api.connectors.DownstreamUri
import api.connectors.DownstreamUri.IfsUri
import api.models.des.IncomeSourceType
import api.models.domain.{BusinessId, Nino, TaxYear}
import v3.retrieveBiss.model.response.RetrieveBISSResponse

sealed trait RetrieveBISSDownstreamUriBuilder[Resp] {
  def buildUri(nino: Nino, businessId: BusinessId, incomeSourceType: IncomeSourceType, taxYear: TaxYear): (DownstreamUri[Resp], Seq[(String, String)])
}

object RetrieveBISSDownstreamUriBuilder {

  case object Api1415 extends RetrieveBISSDownstreamUriBuilder[RetrieveBISSResponse] {

    override def buildUri(nino: Nino,
                          businessId: BusinessId,
                          incomeSourceType: IncomeSourceType,
                          taxYear: TaxYear): (DownstreamUri[RetrieveBISSResponse], Seq[(String, String)]) = {

      val uri: IfsUri[RetrieveBISSResponse] = IfsUri[RetrieveBISSResponse](
        s"income-tax/income-sources/nino/$nino/$incomeSourceType/${taxYear.asDownstream}/biss"
      )

      val queryParams: Seq[(String, String)] = Seq("incomeSourceId" -> s"$businessId")

      (uri, queryParams)
    }

  }

  case object Api1871 extends RetrieveBISSDownstreamUriBuilder[RetrieveBISSResponse] {

    override def buildUri(nino: Nino,
                          businessId: BusinessId,
                          incomeSourceType: IncomeSourceType,
                          taxYear: TaxYear): (DownstreamUri[RetrieveBISSResponse], Seq[(String, String)]) = {

      val uri: IfsUri[RetrieveBISSResponse] = IfsUri[RetrieveBISSResponse](
        s"income-tax/income-sources/${taxYear.asTysDownstream}/$nino/$businessId/$incomeSourceType/biss"
      )

      (uri, Nil)
    }

  }

  case object Api1879 extends RetrieveBISSDownstreamUriBuilder[RetrieveBISSResponse] {

    override def buildUri(nino: Nino,
                          businessId: BusinessId,
                          incomeSourceType: IncomeSourceType,
                          taxYear: TaxYear): (DownstreamUri[RetrieveBISSResponse], Seq[(String, String)]) = {

      val uri: IfsUri[RetrieveBISSResponse] = IfsUri[RetrieveBISSResponse](
        s"income-tax/${taxYear.asTysDownstream}/income-sources/$nino/$businessId/$incomeSourceType/biss"
      )

      (uri, Nil)
    }

  }

  def downstreamUriFor[Resp](taxYear: TaxYear): RetrieveBISSDownstreamUriBuilder[Resp] = {
    val downstreamUriBuilder: RetrieveBISSDownstreamUriBuilder[RetrieveBISSResponse] = taxYear.year match {
      case year if year >= 2026               => Api1879
      case _ if taxYear.useTaxYearSpecificApi => Api1871
      case _                                  => Api1415
    }

    downstreamUriBuilder.asInstanceOf[RetrieveBISSDownstreamUriBuilder[Resp]]
  }

}
