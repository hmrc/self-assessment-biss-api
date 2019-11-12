/*
 * Copyright 2019 HM Revenue & Customs
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

package v1.models.response.selfEmployment

import play.api.libs.functional.syntax._
import play.api.libs.json._
import org.json4s.JsonAST._
import org.json4s.native.Serialization
import org.json4s.{CustomSerializer, DefaultFormats, Formats}

case class RetrieveSelfEmploymentBISSResponse(total: Total,
                                              accountingAdjustments: Option[BigDecimal],
                                              profit: Option[Profit],
                                              loss: Option[Loss]) {

  def toJsonString: String = {
    implicit val formats: Formats = DefaultFormats ++ Seq(BigDecimalSerializer)
    Serialization.write(this)
  }
}

private object BigDecimalSerializer extends CustomSerializer[BigDecimal](format =>
  ({
     case jde: JDecimal => jde.num
   },
   {
     case bd: BigDecimal => JDecimal(bd.setScale(2, BigDecimal.RoundingMode.HALF_UP))
   })
)

object RetrieveSelfEmploymentBISSResponse {

  implicit val reads: Reads[RetrieveSelfEmploymentBISSResponse] = (
    JsPath.read[Total] and
      (JsPath \ "accountingAdjustments").readNullable[BigDecimal] and
      JsPath.readNullable[Profit] and
      JsPath.readNullable[Loss]
    )(RetrieveSelfEmploymentBISSResponse.apply _)

  implicit val writes: OWrites[RetrieveSelfEmploymentBISSResponse] = Json.writes[RetrieveSelfEmploymentBISSResponse]
}

