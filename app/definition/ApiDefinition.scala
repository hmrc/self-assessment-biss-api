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

import play.api.libs.json.{Format, Json, OFormat}
import routing.Version
import utils.enums.Enums

enum APIStatus {
  case ALPHA, BETA, STABLE, DEPRECATED, RETIRED
}

object APIStatus {
  val parser: PartialFunction[String, APIStatus] = Enums.parser(values)

  given Format[APIStatus] = Enums.format(values)

}

case class APIVersion(version: Version, status: APIStatus, endpointsEnabled: Boolean)

object APIVersion {
  implicit val formatAPIVersion: OFormat[APIVersion] = Json.format[APIVersion]
}

case class APIDefinition(name: String,
                         description: String,
                         context: String,
                         categories: Seq[String],
                         versions: Seq[APIVersion],
                         requiresTrust: Option[Boolean]) {

  require(name.nonEmpty, "name is required")
  require(context.nonEmpty, "context is required")
  require(categories.nonEmpty, "at least one category is required")
  require(description.nonEmpty, "description is required")
  require(versions.nonEmpty, "at least one version is required")
  require(uniqueVersions, "version numbers must be unique")

  private def uniqueVersions = {
    !versions.map(_.version).groupBy(identity).view.mapValues(_.size).exists(_._2 > 1)
  }

}

object APIDefinition {
  implicit val formatAPIDefinition: OFormat[APIDefinition] = Json.format[APIDefinition]
}

case class Definition(api: APIDefinition)

object Definition {
  implicit val formatDefinition: OFormat[Definition] = Json.format[Definition]
}
