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

package v3.def1

import api.models.errors.{BusinessIdFormatError, InternalError, MtdError, NinoFormatError, NotFoundError, RuleNoIncomeSubmissionsExist, RuleTaxYearNotSupportedError, RuleTaxYearRangeInvalidError, TaxYearFormatError, TypeOfBusinessFormatError}
import api.services.{AuditStub, AuthStub, DownstreamStub, MtdIdLookupStub}
import play.api.http.HeaderNames._
import play.api.http.MimeTypes
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import play.api.test.Helpers.AUTHORIZATION
import support.IntegrationBaseSpec
import v3.fixtures.RetrieveBISSFixture

class RetrieveBissISpec extends IntegrationBaseSpec with RetrieveBISSFixture {

  "Calling the retrieve BISS endpoint" should {
    "return a valid response with status OK" when {
      "valid request is made" in new NonTysTest {
        DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, queryParams, OK, downstreamResponseJsonFull)

        val response: WSResponse = await(request.get())

        response.json shouldBe responseJsonFull
        response.status shouldBe OK
        response.header(CONTENT_TYPE) shouldBe Some(MimeTypes.JSON)
      }

      "valid TYS request is made" in new TysTest {
        DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, OK, downstreamResponseJsonFull)

        val response: WSResponse = await(request.get())

        response.json shouldBe responseJsonFull
        response.status shouldBe OK
        response.header(CONTENT_TYPE) shouldBe Some(MimeTypes.JSON)
      }

      checkWith("self-employment", "self-employment")
      checkWith("uk-property-fhl", "fhl-property-uk")
      checkWith("uk-property-non-fhl", "uk-property")
      checkWith("foreign-property", "foreign-property")
      checkWith("foreign-property-fhl-eea", "fhl-property-eea")

      def checkWith(requestTypeOfBusiness: String, requestIncomeSourceType: String): Unit = {
        s"work for $requestTypeOfBusiness" in new NonTysTest {
          override val typeOfBusiness: String   = requestTypeOfBusiness
          override val incomeSourceType: String = requestIncomeSourceType

          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, queryParams, OK, downstreamResponseJsonMin)

          val response: WSResponse = await(request.get())

          response.json shouldBe responseJsonMin
          response.status shouldBe OK
          response.header(CONTENT_TYPE) shouldBe Some(MimeTypes.JSON)
        }

        s"work for $requestTypeOfBusiness (TYS)" in new TysTest {
          override val typeOfBusiness: String   = requestTypeOfBusiness
          override val incomeSourceType: String = requestIncomeSourceType

          DownstreamStub.onSuccess(DownstreamStub.GET, downstreamUrl, OK, downstreamResponseJsonMin)

          val response: WSResponse = await(request.get())

          response.json shouldBe responseJsonMin
          response.status shouldBe OK
          response.header(CONTENT_TYPE) shouldBe Some(MimeTypes.JSON)
        }
      }
    }

    "return error according to spec" when {
      def validationErrorTest(requestNino: String,
                              requestTaxYear: String,
                              requestBusinessId: String,
                              requestTypeOfBusiness: String,
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new NonTysTest {
          override val taxYear: String        = requestTaxYear
          override val nino: String           = requestNino
          override val businessId: String     = requestBusinessId
          override val typeOfBusiness: String = requestTypeOfBusiness

          val response: WSResponse = await(request.get())

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
        s"downstream returns an $downstreamCode error and status $downstreamStatus" in new NonTysTest {
          DownstreamStub.onError(DownstreamStub.GET, downstreamUrl, queryParams, downstreamStatus, errorBody(downstreamCode))

          val response: WSResponse = await(request.get())

          response.json shouldBe Json.toJson(expectedBody)
          response.status shouldBe expectedStatus
          response.header(CONTENT_TYPE) shouldBe Some(MimeTypes.JSON)
        }
      }

      val downstreamInput = Seq(
        (BAD_REQUEST, "INVALID_IDVALUE", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_TAXYEAR", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_CORRELATIONID", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_INCOMESOURCETYPE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
        (UNPROCESSABLE_ENTITY, "INCOME_SUBMISSIONS_NOT_EXIST", BAD_REQUEST, RuleNoIncomeSubmissionsExist),
        (UNPROCESSABLE_ENTITY, "INVALID_ACCOUNTING_PERIOD", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "INVALID_QUERY_PARAM", INTERNAL_SERVER_ERROR, InternalError),
        (NOT_FOUND, "NOT_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, InternalError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_REQUEST", INTERNAL_SERVER_ERROR, InternalError)
      )

      val tysInput = Seq(
        (BAD_REQUEST, "INVALID_TAX_YEAR", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "INVALID_INCOMESOURCE_ID", BAD_REQUEST, BusinessIdFormatError),
        (BAD_REQUEST, "INVALID_CORRELATION_ID", INTERNAL_SERVER_ERROR, InternalError),
        (BAD_REQUEST, "INVALID_TAXABLE_ENTITY_ID", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_INCOME_SOURCETYPE", INTERNAL_SERVER_ERROR, InternalError),
        (UNPROCESSABLE_ENTITY, "TAX_YEAR_NOT_SUPPORTED", BAD_REQUEST, RuleTaxYearNotSupportedError)
      )

      (downstreamInput ++ tysInput).foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

  trait Test {

    val taxYear           = "2020-21"
    val downstreamTaxYear = "2021"
    val nino              = "AA123456A"
    val businessId        = "XAIS12345678913"
    val typeOfBusiness    = "self-employment"
    val incomeSourceType  = "self-employment"

    def downstreamUrl: String

    def request: WSRequest = {
      AuditStub.audit()
      AuthStub.authorised()
      MtdIdLookupStub.ninoFound(nino)
      buildRequest(uri)
        .withHttpHeaders(
          (ACCEPT, "application/vnd.hmrc.2.0+json"),
          (AUTHORIZATION, "Bearer 123") // some bearer token
        )
    }

    def uri: String = s"/$nino/$typeOfBusiness/$taxYear/$businessId"

  }

  trait TysTest extends Test {
    def downstreamUrl: String = s"/income-tax/income-sources/23-24/$nino/$businessId/$incomeSourceType/biss"
  }

  trait NonTysTest extends Test {
    def downstreamUrl: String = s"/income-tax/income-sources/nino/$nino/$incomeSourceType/$downstreamTaxYear/biss"

    def queryParams: Map[String, String] = Map("incomeSourceId" -> businessId)
  }

}
