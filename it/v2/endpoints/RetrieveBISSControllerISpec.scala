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

package v2.endpoints

import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import v2.fixtures.RetrieveBISSFixture
import v2.models.errors._
import v2.stubs.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import play.api.http.HeaderNames._
import play.api.http.MimeTypes

class RetrieveBISSControllerISpec extends IntegrationBaseSpec with RetrieveBISSFixture {

  private trait Test {

    val taxYear = "2020-21"
    val downstreamTaxYear = "2021"
    val nino: String       = "AA123456A"
    val businessId: String = "XAIS12345678913"
    val typeOfBusiness = "self-employment"
    val incomeSourceType = "self-employment"

    def uri: String = s"/$nino/$typeOfBusiness/$taxYear/$businessId"

    def downstreamUrl: String = s"/income-tax/income-sources/nino/$nino/$incomeSourceType/$downstreamTaxYear/biss"

    def request: WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      buildRequest(uri)
        .withHttpHeaders(ACCEPT -> "application/vnd.hmrc.2.0+json")
    }
  }

  "Calling the retrieve BISS endpoint" should {
    "return a valid response with status OK" when {
      "valid request is made" in new Test {
        DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, Map("incomeSourceId" -> businessId), OK, downstreamResponseJsonFull)

        val response: WSResponse = await(request.get)

        response.json shouldBe responseJsonFull
        response.status shouldBe OK
        response.header(CONTENT_TYPE) shouldBe Some(MimeTypes.JSON)
      }

      checkWith("self-employment", "self-employment")
      checkWith("uk-property-fhl", "fhl-property-uk")
      checkWith("uk-property-non-fhl", "uk-property")
      checkWith("foreign-property", "foreign-property")
      checkWith("foreign-property-fhl-eea", "fhl-property-eea")

      def checkWith(requestTypeOfBusiness: String, requestIncomeSourceType: String): Unit =
        s"work for $requestTypeOfBusiness" in new Test {
          override val typeOfBusiness: String = requestTypeOfBusiness
          override val incomeSourceType: String =  requestIncomeSourceType

          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, Map("incomeSourceId" -> businessId), OK, downstreamResponseJsonMin)

          val response: WSResponse = await(request.get)

          response.json shouldBe responseJsonMin
          response.status shouldBe OK
          response.header(CONTENT_TYPE) shouldBe Some(MimeTypes.JSON)
        }
    }

    "return error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestTaxYear: String,
                              requestBusinessId: String,
                              requestTypeOfBusiness: String,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {
          override val taxYear: String = requestTaxYear
          override val nino: String       = requestNino
          override val businessId: String = requestBusinessId
          override val typeOfBusiness: String = requestTypeOfBusiness

          val response: WSResponse = await(request.get)

          response.json shouldBe Json.toJson(expectedBody)
          response.status shouldBe expectedStatus
          response.header(CONTENT_TYPE) shouldBe Some(MimeTypes.JSON)
        }
      }

      val input = Seq(
        ("BadNino", "2020-21", "XAIS12345678913", "self-employment", BAD_REQUEST, NinoFormatError),
        ("AA123456A", "NotATaxYear", "XAIS12345678913", "self-employment", BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", "2010-11", "XAIS12345678913", "self-employment", BAD_REQUEST, RuleTaxYearNotSupportedError),
        ("AA123456A", "2020-22", "XAIS12345678913", "self-employment", BAD_REQUEST, RuleTaxYearRangeInvalidError),
        ("AA123456A", "2018-19", "BadBusinessId", "self-employment", BAD_REQUEST, BusinessIdFormatError),
        ("AA123456A", "2018-19", "XAIS12345678913", "not-business-type", BAD_REQUEST, TypeOfBusinessFormatError)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "a downstream service error" when {
      def errorBody(code: String): String =
        s"""{
           |  "code": "$code",
           |  "reason": "downstream message"
           |}""".stripMargin

      def serviceErrorTest(downstreamStatus: Int, downstreamCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"downstream returns an $downstreamCode error and status $downstreamStatus" in new Test {
          DownstreamStub.onError(DownstreamStub.GET, downstreamUrl, Map("incomeSourceId" -> businessId), downstreamStatus, errorBody(downstreamCode))

          val response: WSResponse = await(request.get)

          response.json shouldBe Json.toJson(expectedBody)
          response.status shouldBe expectedStatus
          response.header(CONTENT_TYPE) shouldBe Some(MimeTypes.JSON)
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_IDVALUE", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TAXYEAR", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_INCOMESOURCETYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
        (UNPROCESSABLE_ENTITY, "INCOME_SUBMISSIONS_NOT_EXIST", FORBIDDEN, RuleNoIncomeSubmissionsExist),
        (UNPROCESSABLE_ENTITY, "INVALID_ACCOUNTING_PERIOD", INTERNAL_SERVER_ERROR, DownstreamError),
        (UNPROCESSABLE_ENTITY, "INVALID_QUERY_PARAM", INTERNAL_SERVER_ERROR, DownstreamError),
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_REQUEST", INTERNAL_SERVER_ERROR, DownstreamError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }
}
