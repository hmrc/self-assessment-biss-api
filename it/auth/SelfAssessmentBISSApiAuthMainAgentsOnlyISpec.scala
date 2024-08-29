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

package auth

import api.services.DownstreamStub
import play.api.http.Status._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws.{WSRequest, WSResponse}

class SelfAssessmentBISSApiAuthMainAgentsOnlyISpec extends AuthMainAgentsOnlyISpec {

  val callingApiVersion = "2.0"

  val supportingAgentsNotAllowedEndpoint = "retrieve-biss"

  val taxYear = "2020-21"
  val downstreamTaxYear = "2021"
  val businessId = "XAIS12345678913"
  val typeOfBusiness = "self-employment"
  val incomeSourceType = "self-employment"

  val mtdUrl = s"/$nino/$typeOfBusiness/$taxYear/$businessId"

  def sendMtdRequest(request: WSRequest): WSResponse = await(request.get())

  val downstreamUri: String =
    s"/income-tax/income-sources/nino/$nino/$incomeSourceType/$downstreamTaxYear/biss"

  override val downstreamHttpMethod: DownstreamStub.HTTPMethod = DownstreamStub.GET

  override val expectedMtdSuccessStatus: Int = OK

  val maybeDownstreamResponseJson: Option[JsValue] = Some(
    Json.parse(
      """
        |{
        | "incomeSourceId": "XAIS12345678913",
        | "totalIncome": 1.25,
        | "totalExpenses": 2.25,
        | "netProfit": 3.25,
        | "netLoss": 4.25,
        | "totalAdditions": 5.25,
        | "totalDeductions": 6.25,
        | "accountingAdjustments": 7.25,
        | "taxableProfit": 8.25,
        | "taxableLoss": 9.25
        |}
    """.stripMargin)
  )





}
