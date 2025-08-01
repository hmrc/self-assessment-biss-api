/*
 * Copyright 2025 HM Revenue & Customs
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

import sbt.*

object AppDependencies {

  val bootstrapPlayVersion = "9.18.0"

  val compile: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"                  %% "bootstrap-backend-play-30" % bootstrapPlayVersion,
    "org.typelevel"                %% "cats-core"                 % "2.13.0",
    "com.github.jknack"             % "handlebars"                % "4.3.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"         %% "bootstrap-test-play-30" % bootstrapPlayVersion,
    "org.scalatestplus"   %% "scalacheck-1-18"        % "3.2.19.0",
    "org.scalamock"       %% "scalamock"              % "7.4.0"
  ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = Seq(
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.19.2",
    "io.swagger.parser.v3"         % "swagger-parser-v3"     % "2.1.31"
  ).map(_ % Test)

}
