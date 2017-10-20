/*
 * Copyright 2017 HM Revenue & Customs
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

package helpers.servicemocks

import helpers.{IntegrationTestConstants, WiremockHelper}
import models.{BusinessDetailsModel, BusinessModel, ReportDeadlinesModel}
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}

object SelfAssessmentStub {

  val businessDetailsUrl: String => String = nino => s"/ni/$nino/self-employments"
  val propertyDetailsUrl: String => String = nino => s"/ni/$nino/uk-properties"
  val businessReportDeadlinesUrl: (String, String) => String = (nino, selfEmploymentId) => s"/ni/$nino/self-employments/$selfEmploymentId/obligations"
  val propertyReportDeadlinesUrl: String => String = nino => s"/ni/$nino/uk-properties/obligations"

  //Income Source Details
  def stubGetBusinessDetails(nino: String, businessDetails: JsValue) : Unit =
    WiremockHelper.stubGet(businessDetailsUrl(nino), Status.OK, businessDetails.toString())

  def stubGetNoBusinessDetails(nino: String) : Unit =
    WiremockHelper.stubGet(businessDetailsUrl(nino), Status.OK, "[]")

  def stubGetPropertyDetails(nino: String, propertyDetails: JsValue) : Unit =
    WiremockHelper.stubGet(propertyDetailsUrl(nino), Status.OK, propertyDetails.toString())

  def stubGetNoPropertyDetails(nino: String) : Unit =
    WiremockHelper.stubGet(propertyDetailsUrl(nino), Status.NOT_FOUND, "{}")

  //ReportDeadlines
  def stubGetBusinessReportDeadlines(nino: String, selfEmploymentId: String, business: ReportDeadlinesModel): Unit =
    WiremockHelper.stubGet(businessReportDeadlinesUrl(nino, selfEmploymentId), Status.OK, Json.toJson(business).toString())

  def stubGetPropertyReportDeadlines(nino: String, property: ReportDeadlinesModel): Unit =
    WiremockHelper.stubGet(propertyReportDeadlinesUrl(nino), Status.OK, Json.toJson(property).toString())

  def stubPropertyReportDeadlinesError(nino: String): Unit =
    WiremockHelper.stubGet(propertyReportDeadlinesUrl(nino), Status.INTERNAL_SERVER_ERROR, "ISE")

  def stubBusinessReportDeadlinesError(nino: String, selfEmploymentId: String): Unit =
    WiremockHelper.stubGet(businessReportDeadlinesUrl(nino, selfEmploymentId), Status.INTERNAL_SERVER_ERROR, "ISE")


  // Verifications
  def verifyGetBusinessReportDeadlines(nino: String, selfEmploymentId: String): Unit =
    WiremockHelper.verifyGetWithHeader(businessReportDeadlinesUrl(nino, selfEmploymentId), "Accept", "application/vnd.hmrc.1.0+json")

  def verifyGetPropertyReportDeadlines(nino: String): Unit =
    WiremockHelper.verifyGetWithHeader(propertyReportDeadlinesUrl(nino), "Accept", "application/vnd.hmrc.1.0+json")

  def verifyGetBusinessDetails(nino: String): Unit =
    WiremockHelper.verifyGetWithHeader(businessDetailsUrl(nino), "Accept", "application/vnd.hmrc.1.0+json")

  def verifyGetPropertyDetails(nino: String): Unit =
    WiremockHelper.verifyGetWithHeader(propertyDetailsUrl(nino), "Accept", "application/vnd.hmrc.1.0+json")
}

