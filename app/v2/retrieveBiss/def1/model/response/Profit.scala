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

package v2.retrieveBiss.def1.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Profit(net: BigDecimal, taxable: BigDecimal)

object Profit {

  implicit val reads: Reads[Profit] = (
    (JsPath \ "netProfit").read[BigDecimal] and
      (JsPath \ "taxableProfit").read[BigDecimal]
    ) (Profit.apply _)

  implicit val writes: OWrites[Profit] = Json.writes[Profit]
}
