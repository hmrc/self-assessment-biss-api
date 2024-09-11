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

package v3.retrieveBiss.def1.model.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Json, OWrites, Reads}

case class Total(income: BigDecimal,
                 expenses: BigDecimal,
                 additions: Option[BigDecimal],
                 deductions: Option[BigDecimal],
                 accountingAdjustments: Option[BigDecimal])

object Total {

  implicit val reads: Reads[Total] = (
    (JsPath \ "totalIncome").read[BigDecimal] and
      (JsPath \ "totalExpenses").read[BigDecimal] and
      (JsPath \ "totalAdditions").readNullable[BigDecimal] and
      (JsPath \ "totalDeductions").readNullable[BigDecimal] and
      (JsPath \ "accountingAdjustments").readNullable[BigDecimal]
    ) (Total.apply _)

  implicit val writes: OWrites[Total] = Json.writes[Total]
}
