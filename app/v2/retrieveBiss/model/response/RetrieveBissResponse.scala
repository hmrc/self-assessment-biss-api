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

package v2.retrieveBiss.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v2.retrieveBiss.def1.model.response.{Loss, Profit, Total}

sealed trait RetrieveBissResponse

object RetrieveBissResponse {

  implicit val writes: OWrites[RetrieveBissResponse] = { case def1: Def1_RetrieveBissResponse =>
    Json.toJsObject(def1)
  }

}

case class Def1_RetrieveBissResponse(total: Total, profit: Profit, loss: Loss) extends RetrieveBissResponse

object Def1_RetrieveBissResponse {

  implicit val reads: Reads[Def1_RetrieveBissResponse] = (
    JsPath.read[Total] and
      JsPath.read[Profit] and
      JsPath.read[Loss]
  )(Def1_RetrieveBissResponse.apply)

  implicit val writes: OWrites[Def1_RetrieveBissResponse] = Json.writes[Def1_RetrieveBissResponse]

}
