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

package definition

import definition.APIStatus.ALPHA
import play.api.libs.json.{JsValue, Json}
import routing.Version3
import support.UnitSpec

class ApiDefinitionSpec extends UnitSpec {

  val apiVersion: APIVersion       = APIVersion(Version3, ALPHA, endpointsEnabled = false)
  val apiDefinition: APIDefinition = APIDefinition("b", "c", "d", Seq("e"), Seq(apiVersion), Some(false))
  val definition: Definition       = Definition(apiDefinition)

  private val apiVersionJson: JsValue = Json.parse(s"""
       |{
       | "version": "3.0",
       |"status": "ALPHA",
       |"endpointsEnabled": false
       |}
       |""".stripMargin)

  private val apiDefinitionJson: JsValue = Json.parse(s"""{
       |"name": "b",
       |"description": "c",
       |"context": "d",
       |"categories": ["e"],
       |"versions": [$apiVersionJson],
       |"requiresTrust": false
       |}
       |""".stripMargin)

  private val definitionJson: JsValue = Json.parse(s"""{
       |"api": $apiDefinitionJson
       |}
       |""".stripMargin)

  "Definition" when {
    "the full model is present" should {
      "correctly write the model to json" in {
        Json.toJson(definition) shouldBe definitionJson
      }
    }

    "the full Json is present" should {
      "correctly read JSON to a model" in {
        definitionJson.as[Definition] shouldBe definition
      }
    }
  }

  "APIDefinition" when {
    "the 'name' parameter is empty" should {
      "throw an 'IllegalArgumentException'" in {
        assertThrows[IllegalArgumentException](
          apiDefinition.copy(name = "")
        )
      }
    }
  }

  "the 'description' parameter is empty" should {
    "throw an 'IllegalArgumentException'" in {
      assertThrows[IllegalArgumentException](
        apiDefinition.copy(description = "")
      )
    }
  }

  "the 'context' parameter is empty" should {
    "throw an 'IllegalArgumentException'" in {
      assertThrows[IllegalArgumentException](
        apiDefinition.copy(context = "")
      )
    }
  }

  "the 'categories' parameter is empty" should {
    "throw an 'IllegalArgumentException'" in {
      assertThrows[IllegalArgumentException](
        apiDefinition.copy(categories = Seq())
      )
    }
  }

  "the 'versions' parameter is empty" should {
    "throw an 'IllegalArgumentException'" in {
      assertThrows[IllegalArgumentException](
        apiDefinition.copy(versions = Seq(apiVersion, apiVersion))
      )
    }
  }

  "the 'versions' parameter is not unique" should {
    "throw an 'IllegalArgumentException'" in {
      assertThrows[IllegalArgumentException](
        apiDefinition.copy(versions = Seq())
      )
    }
  }

}
