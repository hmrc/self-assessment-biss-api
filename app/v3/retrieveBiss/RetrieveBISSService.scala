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

package v3.retrieveBiss

import api.controllers.RequestContext
import api.services.{BaseService, ServiceOutcome}
import cats.implicits._
import v3.retrieveBiss.downstreamErrorMapping.RetrieveBISSDownstreamErrorMapping.errorMapFor
import v3.retrieveBiss.model.request.RetrieveBISSRequestData
import v3.retrieveBiss.model.response.RetrieveBISSResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RetrieveBISSService @Inject() (connector: RetrieveBISSConnector) extends BaseService {

  def retrieveBiss(
      request: RetrieveBISSRequestData)(implicit ctx: RequestContext, ec: ExecutionContext): Future[ServiceOutcome[RetrieveBISSResponse]] = {

    connector
      .retrieveBiss(request)
      .map(_.leftMap(mapDownstreamErrors(errorMapFor(request.taxYear).errorMap)))
  }

}
