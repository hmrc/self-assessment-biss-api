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

package v1.models.response

import org.json4s.{DefaultFormats, Formats}
import org.json4s.native.Serialization
import play.api.libs.json.{JsPath, Json, OWrites, Reads}
import v1.models.response.common.{Loss, Profit, Total}
import play.api.libs.functional.syntax._

case class RetrieveForeignPropertyBISSResponse(total: Total,
                                               profit: Option[Profit],
                                               loss: Option[Loss]) {

  def toJsonString: String = {
    implicit val formats: Formats = DefaultFormats ++ Seq(BigDecimalSerializer)
    Serialization.write(this)
  }

}

object RetrieveForeignPropertyBISSResponse {

  implicit val reads: Reads[RetrieveForeignPropertyBISSResponse] = (
    JsPath.read[Total] and
      JsPath.readNullable[Profit].map{
        case Some(Profit(None, None)) => None
        case obj => obj
      } and
      JsPath.readNullable[Loss].map{
        case Some(Loss(None, None)) => None
        case obj => obj
      }
    )(RetrieveForeignPropertyBISSResponse.apply _)

  implicit val writes: OWrites[RetrieveForeignPropertyBISSResponse] = Json.writes[RetrieveForeignPropertyBISSResponse]

}