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

package api.controllers

import api.controllers.validators.Validator
import api.models.errors.{ErrorWrapper, InternalError}
import api.models.outcomes.ResponseWrapper
import api.services.ServiceOutcome
import cats.data.EitherT
import cats.data.Validated.Valid
import cats.implicits._
import config.AppConfig
import config.Deprecation.Deprecated
import play.api.http.Status
import play.api.libs.json.{JsValue, Writes}
import play.api.mvc.Result
import play.api.mvc.Results.InternalServerError
import routing.Version
import utils.DateUtils.longDateTimestampGmt
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

trait RequestHandler {

  def handleRequest()(implicit
      ctx: RequestContext,
      request: UserRequest[?],
      ec: ExecutionContext,
      appConfig: AppConfig
  ): Future[Result]

}

object RequestHandler {

  def withValidator[Input](validator: Validator[Input]): ValidatorOnlyBuilder[Input] =
    new ValidatorOnlyBuilder[Input](validator)

  class ValidatorOnlyBuilder[Input] private[RequestHandler] (validator: Validator[Input]) {

    def withService[Output](serviceFunction: Input => Future[ServiceOutcome[Output]]): RequestHandlerBuilder[Input, Output] =
      RequestHandlerBuilder(validator, serviceFunction)

  }

  case class RequestHandlerBuilder[Input, Output] private[RequestHandler] (
      validator: Validator[Input],
      service: Input => Future[ServiceOutcome[Output]],
      errorHandling: ErrorHandling = ErrorHandling.Default,
      resultCreator: ResultCreator[Input, Output] = ResultCreator.noContent[Input, Output](),
      auditHandler: Option[AuditHandler] = None,
      modelHandler: Option[Output => Output] = None
  ) extends RequestHandler {

    def handleRequest()(implicit ctx: RequestContext, request: UserRequest[?], ec: ExecutionContext, appConfig: AppConfig): Future[Result] =
      Delegate.handleRequest()

    def withErrorHandling(errorHandling: ErrorHandling): RequestHandlerBuilder[Input, Output] =
      copy(errorHandling = errorHandling)

    def withAuditing(auditHandler: AuditHandler): RequestHandlerBuilder[Input, Output] =
      copy(auditHandler = Some(auditHandler))

    def withModelHandling(modelHandler: Output => Output): RequestHandlerBuilder[Input, Output] =
      copy(modelHandler = Option(modelHandler))

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.plainJson(successStatus))
      * }}}
      */
    def withPlainJsonResult(successStatus: Int = Status.OK)(implicit ws: Writes[Output]): RequestHandlerBuilder[Input, Output] =
      withResultCreator(ResultCreator.plainJson(successStatus))

    /** Shorthand for
      * {{{
      * withResultCreator(ResultCreator.noContent)
      * }}}
      */
    def withNoContentResult(successStatus: Int = Status.NO_CONTENT): RequestHandlerBuilder[Input, Output] =
      withResultCreator(ResultCreator.noContent(successStatus))

    def withResultCreator(resultCreator: ResultCreator[Input, Output]): RequestHandlerBuilder[Input, Output] =
      copy(resultCreator = resultCreator)

    // Scoped as a private delegate so as to keep the logic completely separate from the configuration
    private object Delegate extends RequestHandler with Logging with RequestContextImplicits {

      implicit class Response(result: Result)(implicit appConfig: AppConfig, apiVersion: Version) {

        private def withDeprecationHeaders: List[(String, String)] = {

          appConfig.deprecationFor(apiVersion) match {
            case Valid(Deprecated(deprecatedOn, Some(sunsetDate))) =>
              List(
                "Deprecation" -> longDateTimestampGmt(deprecatedOn),
                "Sunset"      -> longDateTimestampGmt(sunsetDate),
                "Link"        -> appConfig.apiDocumentationUrl
              )
            case Valid(Deprecated(deprecatedOn, None)) =>
              List(
                "Deprecation" -> longDateTimestampGmt(deprecatedOn),
                "Link"        -> appConfig.apiDocumentationUrl
              )
            case _ => Nil
          }
        }

        def withApiHeaders(correlationId: String, responseHeaders: (String, String)*): Result = {

          val headers =
            responseHeaders ++
              List(
                "X-CorrelationId" -> correlationId
              ) ++
              withDeprecationHeaders

          result.copy(header = result.header.copy(headers = result.header.headers ++ headers))
        }

      }

      def handleRequest()(implicit
          ctx: RequestContext,
          request: UserRequest[?],
          ec: ExecutionContext,
          appConfig: AppConfig
      ): Future[Result] = {

        logger.info(
          message = s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] " +
            s"with correlationId : ${ctx.correlationId}")

        val result =
          for {
            parsedRequest   <- EitherT.fromEither[Future](validator.validateAndWrapResult())
            serviceResponse <- EitherT(service(parsedRequest))
          } yield doWithContext(ctx.withCorrelationId(serviceResponse.correlationId)) { implicit ctx: RequestContext =>
            handleSuccess(parsedRequest, serviceResponse)
          }

        result.leftMap { errorWrapper =>
          doWithContext(ctx.withCorrelationId(errorWrapper.correlationId)) { implicit ctx: RequestContext =>
            handleFailure(errorWrapper)
          }
        }.merge
      }

      private def doWithContext[A](ctx: RequestContext)(f: RequestContext => A): A = f(ctx)

      private def handleSuccess(parsedRequest: Input, serviceResponse: ResponseWrapper[Output])(implicit
          ctx: RequestContext,
          request: UserRequest[?],
          ec: ExecutionContext,
          appConfig: AppConfig
      ): Result = {

        implicit val apiVersion: Version = Version(request)

        logger.info(
          s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] - " +
            s"Success response received with CorrelationId: ${ctx.correlationId}")

        val resultWrapper = resultCreator
          .createResult(parsedRequest, serviceResponse.responseData)

        val result = resultWrapper.asResult.withApiHeaders(ctx.correlationId)
        auditIfRequired(result.header.status, Right(resultWrapper.body))
        result
      }

      private def handleFailure(errorWrapper: ErrorWrapper)(implicit
          ctx: RequestContext,
          request: UserRequest[?],
          ec: ExecutionContext,
          appConfig: AppConfig
      ): Result = {

        implicit val apiVersion: Version = Version(request)

        logger.warn(
          s"[${ctx.endpointLogContext.controllerName}][${ctx.endpointLogContext.endpointName}] - " +
            s"Error response received with CorrelationId: ${ctx.correlationId}")

        val errorResult = errorHandling.errorHandler.applyOrElse(errorWrapper, unhandledError)
        val result      = errorResult.withApiHeaders(ctx.correlationId)
        auditIfRequired(result.header.status, Left(errorWrapper))
        result
      }

      private def unhandledError(errorWrapper: ErrorWrapper)(implicit endpointLogContext: EndpointLogContext): Result = {
        logger.error(
          s"[${endpointLogContext.controllerName}][${endpointLogContext.endpointName}] - " +
            s"Unhandled error: $errorWrapper")
        InternalServerError(InternalError.asJson)
      }

      private def auditIfRequired(httpStatus: Int, response: Either[ErrorWrapper, Option[JsValue]])(implicit
          ctx: RequestContext,
          request: UserRequest[?],
          ec: ExecutionContext): Unit =
        auditHandler.foreach { creator =>
          creator.performAudit(request.userDetails, httpStatus, response)
        }

    }

  }

}
