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

package api.controllers.validators.resolvers

import api.models.errors.RuleIncorrectOrEmptyBodyError
import api.models.utils.JsonErrorValidators
import cats.data.Validated.{Invalid, Valid}
import play.api.libs.json.{Json, OFormat}
import support.UnitSpec
import utils.EmptinessChecker
import utils.EmptinessChecker.field

class ResolveNonEmptyJsonObjectSpec extends UnitSpec with JsonErrorValidators {

  case class TestDataObject(field1: String, field2: String, oneOf1: Option[String] = None, oneOf2: Option[String] = None)
  case class TestDataWrapper(arrayField: Seq[TestDataObject])

  implicit val testDataObjectFormat: OFormat[TestDataObject]   = Json.format[TestDataObject]
  implicit val testDataWrapperFormat: OFormat[TestDataWrapper] = Json.format[TestDataWrapper]

  // at least one of oneOf1 and oneOf2 must be included:
  implicit val emptinessChecker: EmptinessChecker[TestDataObject] = EmptinessChecker.use { o =>
    List(
      field("oneOf1", o.oneOf1),
      field("oneOf2", o.oneOf2)
    )
  }

  private val resolveTestDataObject = new ResolveNonEmptyJsonObject[TestDataObject]()

  private val resolveTestDataWrapper = new ResolveNonEmptyJsonObject[TestDataWrapper]()

  "ResolveNonEmptyJsonObject" should {
    "return the object" when {
      "given a valid JSON object" in {
        withClue("Uses the implicit emptinessChecker from above, which requires oneOf1 and oneOf2") {
          val json   = Json.parse("""{ "field1" : "Something", "field2" : "SomethingElse", "oneOf1": "another1", "oneOf2": "another2" }""")
          val result = resolveTestDataObject(json)
          result shouldBe Valid(TestDataObject("Something", "SomethingElse", Some("another1"), Some("another2")))
        }
      }
    }

    "return an error " when {
      "a required field is missing" in {
        val json = Json.parse("""{ "field1" : "Something" }""")

        val result = resolveTestDataObject(json)
        result shouldBe Invalid(
          List(
            RuleIncorrectOrEmptyBodyError.withPath("/field2")
          ))
      }

      "a required field is missing in an array object" in {
        val json = Json.parse("""{ "arrayField" : [{ "field1" : "Something" }]}""")

        val result = resolveTestDataWrapper(json)
        result.shouldBe(
          Invalid(
            List(
              RuleIncorrectOrEmptyBodyError.withPath("/arrayField/0/field2")
            )))
      }

      "a required field is missing in multiple array objects" in {
        val json = Json.parse("""
            |{
            |  "arrayField" : [
            |    { "field1" : "Something" },
            |    { "field1" : "Something" }
            |  ]
            |}
            |""".stripMargin)

        val result = resolveTestDataWrapper(json)
        result shouldBe Invalid(
          List(
            RuleIncorrectOrEmptyBodyError.withPaths(
              List(
                "/arrayField/0/field2",
                "/arrayField/1/field2"
              ))
          ))
      }

      "an empty body is submitted" in {
        val json = Json.parse("""{}""")

        val result = resolveTestDataObject(json)
        result shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError))
      }

      "a non-empty body is supplied without any expected fields" in {
        val json = Json.parse("""{"field": "value"}""")

        val result = resolveTestDataObject(json)
        result shouldBe Invalid(
          List(
            RuleIncorrectOrEmptyBodyError.withPaths(List("/field1", "/field2"))
          ))
      }

      "a field is supplied with the wrong data type" in {
        val json = Json.parse("""{"field1": true, "field2": "value"}""")

        val result = resolveTestDataObject(json)
        result shouldBe Invalid(
          List(
            RuleIncorrectOrEmptyBodyError.withPath("/field1")
          ))
      }

      "detect empty objects" in {
        val json = Json.parse("""{ "field1" : "Something", "field2" : "SomethingElse" }""")

        val result = resolveTestDataObject(json)
        result shouldBe Invalid(List(RuleIncorrectOrEmptyBodyError))
      }

      "detect empty arrays" in {
        val json   = Json.parse("""{ "arrayField": [] }""")
        val result = resolveTestDataWrapper(json)

        result shouldBe Invalid(
          List(
            RuleIncorrectOrEmptyBodyError.withPath("/arrayField")
          ))
      }

      "return no error when all objects are non-empty" in {
        val json = Json.parse("""{ "field1" : "Something", "field2" : "SomethingElse", "oneOf1": "another1" }""")

        val result = resolveTestDataObject(json)
        result shouldBe Valid(
          TestDataObject("Something", "SomethingElse", Some("another1"))
        )
      }
    }
  }

}
