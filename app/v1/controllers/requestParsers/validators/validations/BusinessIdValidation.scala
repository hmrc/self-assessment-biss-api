/*
 * Copyright 2021 HM Revenue & Customs
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

package v1.controllers.requestParsers.validators.validations

import v1.models.errors.{BusinessIdFormatError, MtdError, RuleForeignBusinessIdError}

object BusinessIdValidation {

  private val regex = "^X[A-Z0-9]{1}IS[0-9]{11}$"

  def validate(businessId: Option[String]): List[MtdError] = businessId match {
    case Some(i) if(i.matches(regex)) =>  NoValidationErrors
    case Some(_) =>  List(BusinessIdFormatError)
    case None => List(RuleForeignBusinessIdError)
  }

}
