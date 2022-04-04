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

package v1.endpoints

import java.time.LocalDate

import com.github.tomakehurst.wiremock.stubbing.StubMapping
import fixtures.RetrieveForeignPropertyFixtures._
import play.api.http.HeaderNames.ACCEPT
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.libs.ws.{WSRequest, WSResponse}
import support.IntegrationBaseSpec
import utils.DateUtils
import v1.models.errors._
import v1.models.requestData.DesTaxYear
import v1.stubs.{AuditStub, AuthStub, DesStub, MtdIdLookupStub}

class RetrieveForeignPropertyBISSControllerISpec extends IntegrationBaseSpec {

  private trait Test {

    val nino                           = "AA123456A"
    val taxYear: Option[String]        = Some("2019-20")
    val typeOfBusiness: Option[String] = Some("foreign-property")
    val desTaxYear: DesTaxYear         = DesTaxYear("2020")
    val businessId                     = "XAIS12345678910"

    def uri: String = s"/$nino/foreign-property"

    def desUrl: String = s"/income-tax/income-sources/nino/$nino/foreign-property/${desTaxYear.toString}/biss"

    def setupStubs(): StubMapping

    def request: WSRequest = {
      val queryParams: Seq[(String, String)] = (taxYear, typeOfBusiness, businessId) match {
        case (Some(x), Some(y), z) => Seq("taxYear" -> x, "typeOfBusiness" -> y, "businessId" -> z)
        case (None, Some(y), z)    => Seq("typeOfBusiness" -> y, "businessId" -> z)
        case (Some(x), None, z)    => Seq("taxYear" -> x, "businessId" -> z)
        case (None, None, z)       => Seq("businessId" -> z)
      }

      setupStubs()
      buildRequest(uri)
        .addQueryStringParameters(queryParams: _*)
        .withHttpHeaders((ACCEPT, "application/vnd.hmrc.1.0+json"))
    }

  }

  "Calling the retrieve foreign property BISS endpoint" should {

    "return a valid response with status OK" when {

      "valid request is made" in new Test {

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, Map("incomesourceid" -> businessId), OK, desResponse)
        }
        val response: WSResponse = await(request.get)

        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe mtdResponse
      }

      "valid request is made without a tax year" in new Test {

        override val taxYear: Option[String] = None
        override val desTaxYear: DesTaxYear  = DateUtils.getDesTaxYear(LocalDate.now())

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, Map("incomesourceid" -> businessId), OK, desResponse)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe mtdResponse
      }

      "valid request is made and des returns only mandatory data" in new Test {

        override val taxYear: Option[String] = None
        override val desTaxYear: DesTaxYear  = DateUtils.getDesTaxYear(LocalDate.now())

        override def setupStubs(): StubMapping = {
          AuditStub.audit()
          AuthStub.authorised()
          MtdIdLookupStub.ninoFound(nino)
          DesStub.onSuccess(DesStub.GET, desUrl, Map("incomesourceid" -> businessId), OK, desResponseWithOnlyRequiredData)
        }

        val response: WSResponse = await(request.get)
        response.status shouldBe OK
        response.header("Content-Type") shouldBe Some("application/json")
        response.json shouldBe mtdResponseWithOnlyRequiredData
      }
    }

    "return error according to spec" when {

      def validationErrorTest(requestNino: String,
                              requestTaxYear: Option[String],
                              requestBusiness: Option[String],
                              expectedStatus: Int,
                              expectedBody: MtdError): Unit = {
        s"validation fails with ${expectedBody.code} error" in new Test {

          override val nino: String                   = requestNino
          override val taxYear: Option[String]        = requestTaxYear
          override val typeOfBusiness: Option[String] = requestBusiness

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
          }

          val response: WSResponse = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        ("AA1123A", None, Some("foreign-property-fhl-eea"), BAD_REQUEST, NinoFormatError),
        ("AA123456A", Some("20177"), Some("foreign-property-fhl-eea"), BAD_REQUEST, TaxYearFormatError),
        ("AA123456A", Some("2018-19"), Some("123456789"), BAD_REQUEST, TypeOfBusinessFormatError),
        ("AA123456A", Some("2018-19"), None, BAD_REQUEST, RuleTypeOfBusinessError)
      )

      input.foreach(args => (validationErrorTest _).tupled(args))
    }

    "des service error" when {

      def errorBody(code: String): String =
        s"""{
           |  "code": "$code",
           |  "reason": "des message"
           |}""".stripMargin

      def serviceErrorTest(desStatus: Int, desCode: String, expectedStatus: Int, expectedBody: MtdError): Unit = {
        s"des returns an $desCode error and status $desStatus" in new Test {

          override def setupStubs(): StubMapping = {
            AuditStub.audit()
            AuthStub.authorised()
            MtdIdLookupStub.ninoFound(nino)
            DesStub.onError(DesStub.GET, desUrl, Map(), desStatus, errorBody(desCode))
          }

          val response: WSResponse = await(request.get)
          response.status shouldBe expectedStatus
          response.json shouldBe Json.toJson(expectedBody)
          response.header("Content-Type") shouldBe Some("application/json")
        }
      }

      val input = Seq(
        (BAD_REQUEST, "INVALID_IDVALUE", BAD_REQUEST, NinoFormatError),
        (BAD_REQUEST, "INVALID_IDTYPE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_INCOMESOURCEID", BAD_REQUEST, BusinessIdFormatError),
        (BAD_REQUEST, "INVALID_TAXYEAR", BAD_REQUEST, TaxYearFormatError),
        (BAD_REQUEST, "INVALID_INCOMESOURCETYPE", BAD_REQUEST, TypeOfBusinessFormatError),
        (BAD_REQUEST, "NOT_FOUND", NOT_FOUND, NotFoundError),
        (INTERNAL_SERVER_ERROR, "SERVER_ERROR", INTERNAL_SERVER_ERROR, DownstreamError),
        (SERVICE_UNAVAILABLE, "SERVICE_UNAVAILABLE", INTERNAL_SERVER_ERROR, DownstreamError),
        (BAD_REQUEST, "INVALID_REQUEST", INTERNAL_SERVER_ERROR, DownstreamError)
      )

      input.foreach(args => (serviceErrorTest _).tupled(args))
    }
  }

}
