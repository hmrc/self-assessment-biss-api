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

package routing

import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json._
import play.api.test.FakeRequest
import support.UnitSpec

class VersionSpec extends UnitSpec {

  "serialized to Json" must {
    "return the expected Json output" in {
      val version: Version = Version2
      val expected         = Json.parse(""" "2.0" """)
      val result           = Json.toJson(version)
      result shouldBe expected
    }
  }

  "deserialized from Json" must {
    "return Version2 for '2.0'" in {
      val json   = Json.parse(""""2.0"""")
      val result = Json.fromJson[Version](json)
      result shouldBe JsSuccess(Version2)
    }

    "return Version3 for '3.0'" in {
      val json   = Json.parse(""""3.0"""")
      val result = Json.fromJson[Version](json)
      result shouldBe JsSuccess(Version3)
    }

    "fail with JsError for unknown version" in {
      val json   = Json.parse(""""4.0"""")
      val result = Json.fromJson[Version](json)
      result shouldBe JsError("Unrecognised version")
    }

    "fail with JsError if not a string" in {
      val json   = Json.parse("123")
      val result = Json.fromJson[Version](json)
      result shouldBe a[JsError]
    }
  }

  "Versions" when {
    "retrieved from a request header" should {
      "return Version2 for valid header" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.2.0+json"))) shouldBe Right(Version2)
      }
      "return Version3 for valid header" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.3.0+json"))) shouldBe Right(Version3)
      }
      "return InvalidHeader when the version header is missing" in {
        Versions.getFromRequest(FakeRequest().withHeaders()) shouldBe Left(InvalidHeader)
      }
      "return VersionNotFound for unrecognised version" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "application/vnd.hmrc.5.0+json"))) shouldBe Left(VersionNotFound)
      }
      "return InvalidHeader for a header format that doesn't match regex" in {
        Versions.getFromRequest(FakeRequest().withHeaders((ACCEPT, "invalidHeaderFormat"))) shouldBe Left(InvalidHeader)
      }
    }
  }

}
