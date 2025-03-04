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
package controllers

import assets.BaseIntegrationTestConstants._
import assets.IncomeSourceIntegrationTestConstants._
import assets.ReportDeadlinesIntegrationTestConstants._
import assets.PreviousObligationsIntegrationTestConstants._
import assets.messages.{ReportDeadlinesMessages => obligationsMessages}
import config.featureswitch.{FeatureSwitching, ObligationsPage, ReportDeadlines}
import helpers.ComponentSpecBase
import helpers.servicemocks.IncomeTaxViewChangeStub
import implicits.ImplicitDateFormatter
import models.reportDeadlines.ObligationsModel
import play.api.http.Status._

class ReportDeadlinesControllerISpec extends ComponentSpecBase {

  "Calling the ReportDeadlinesController" when {

    "the ReportDeadlines Feature is enabled" when {

      unauthorisedTest("/obligations")

      "the obligations feature switch is enabled" when {

        "the user has a eops property income obligation only and no previous obligations" in {
          enable(ReportDeadlines)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, propertyOnlyResponse)

          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(singleObligationEOPSPropertyModel)))

          IncomeTaxViewChangeStub.stubGetPreviousObligationsNotFound(testNino)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)

          verifyReportDeadlinesCall(testNino)

          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title, username and links")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays one eops property income obligation")
          res should have(
            elementTextBySelector("#eops-return-section-0 div div:nth-child(1) div:nth-child(2)")("6 April 2017 to 5 July 2018"),
            elementTextBySelector("#eops-return-section-0 div div:nth-child(2) div:nth-child(2)")("1 January 2018"),
            isElementVisibleById("eops-return-section-1")(expectedValue = false)
          )

          Then("the page displays no previous obligations")
          res should have(
            elementTextByID("no-previous-obligations")(obligationsMessages.noPreviousObligations)
          )
        }

        "the user has a eops property income obligation and previous obligations" in {
          enable(ObligationsPage)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, propertyOnlyResponse)

          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(singleObligationEOPSPropertyModel)))

          IncomeTaxViewChangeStub.stubGetPreviousObligations(testNino, previousObligationsModel)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)

          verifyReportDeadlinesCall(testNino)

          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title, username and links")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays one eops property income obligation")
          res should have(
            elementTextBySelector("#eops-return-section-0 div div:nth-child(1) div:nth-child(2)")("6 April 2017 to 5 July 2018"),
            elementTextBySelector("#eops-return-section-0 div div:nth-child(2) div:nth-child(2)")("1 January 2018"),
            isElementVisibleById("eops-return-section-1")(expectedValue = false)
          )

          Then("the page displays the previous obligations")
          res should have(
            isElementVisibleById("no-previous-obligations")(expectedValue = false),
            elementTextByID("income-source-0")("Tax year - Final check"),
            elementTextByID("obligation-type-0")("Declaration"),
            elementTextByID("date-from-to-0")("1 May 2017 to 1 June 2017"),
            elementTextByID("was-due-on-0")("Was due on 1 July 2017"),
            elementTextByID("submitted-on-date-0")("1 June 2017")
          )
        }

        "the user has no obligations" in {
          enable(ObligationsPage)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, noPropertyOrBusinessResponse)

          IncomeTaxViewChangeStub.stubGetReportDeadlinesNotFound(testNino)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)

          verifyReportDeadlinesCall(testNino)

          Then("the view displays the correct title, username and links")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.title)
          )

          Then("the page displays no obligation dates")
          res should have(
            elementTextByID("p1")(obligationsMessages.noReports)
          )
        }

        "the user has a quarterly property income obligation only and no previous obligations" in {
          enable(ObligationsPage)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, propertyOnlyResponse)

          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(singleObligationQuarterlyModel(testPropertyIncomeId))))

          IncomeTaxViewChangeStub.stubGetPreviousObligationsNotFound(testNino)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)
          verifyReportDeadlinesCall(testNino)
          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title, username and links")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays the property obligation dates")
          res should have(
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(1) div:nth-child(2)")(expectedValue = "6 April 2017 to 5 July 2017"),
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(2) div:nth-child(2)")(expectedValue = "1 January 2018"),
            isElementVisibleById("quarterly-return-section-1")(expectedValue = false)
          )

          Then("the page displays no previous obligations")
          res should have(
            elementTextByID("no-previous-obligations")(obligationsMessages.noPreviousObligations)
          )
        }

        "the user has a quarterly property income obligation and previous obligations" in {
          enable(ObligationsPage)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, propertyOnlyResponse)

          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(singleObligationQuarterlyModel(testPropertyIncomeId))))

          IncomeTaxViewChangeStub.stubGetPreviousObligationsNotFound(testNino)

          IncomeTaxViewChangeStub.stubGetPreviousObligations(testNino, previousObligationsModel)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)
          verifyReportDeadlinesCall(testNino)
          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title, username and links")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays the property obligation dates")
          res should have(
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(1) div:nth-child(2)")(expectedValue = "6 April 2017 to 5 July 2017"),
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(2) div:nth-child(2)")(expectedValue = "1 January 2018"),
            isElementVisibleById("quarterly-return-section-1")(expectedValue = false)
          )

          Then("the page displays the previous obligations")
          res should have(
            isElementVisibleById("no-previous-obligations")(expectedValue = false),
            elementTextByID("income-source-0")("Tax year - Final check"),
            elementTextByID("obligation-type-0")("Declaration"),
            elementTextByID("date-from-to-0")("1 May 2017 to 1 June 2017"),
            elementTextByID("was-due-on-0")("Was due on 1 July 2017"),
            elementTextByID("submitted-on-date-0")("1 June 2017")
          )
        }

        "the user has a quarterly business income obligation only and no previous obligations" in {
          enable(ObligationsPage)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, singleBusinessResponse)

          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(singleObligationQuarterlyModel(testSelfEmploymentId))))

          IncomeTaxViewChangeStub.stubGetPreviousObligationsNotFound(testNino)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)

          verifyReportDeadlinesCall(testNino)

          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title, username and links")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays the property obligation dates")
          res should have(
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(1) div:nth-child(2)")(expectedValue = "6 April 2017 to 5 July 2017"),
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(2) div:nth-child(2)")(expectedValue = "1 January 2018"),
            isElementVisibleById("quarterly-return-section-1")(expectedValue = false)
          )

          Then("the page displays no previous obligations")
          res should have(
            elementTextByID("no-previous-obligations")(obligationsMessages.noPreviousObligations)
          )
        }

        "the user has a quarterly business income obligation and previous obligations" in {
          enable(ObligationsPage)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, singleBusinessResponse)

          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(singleObligationQuarterlyModel(testSelfEmploymentId))))

          IncomeTaxViewChangeStub.stubGetPreviousObligations(testNino, previousObligationsModel)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)

          verifyReportDeadlinesCall(testNino)

          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title, username and links")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays the property obligation dates")
          res should have(
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(1) div:nth-child(2)")(expectedValue = "6 April 2017 to 5 July 2017"),
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(2) div:nth-child(2)")(expectedValue = "1 January 2018"),
            isElementVisibleById("quarterly-return-section-1")(expectedValue = false)
          )

          Then("the page displays the previous obligations")
          res should have(
            isElementVisibleById("no-previous-obligations")(expectedValue = false),
            elementTextByID("income-source-0")("Tax year - Final check"),
            elementTextByID("obligation-type-0")("Declaration"),
            elementTextByID("date-from-to-0")("1 May 2017 to 1 June 2017"),
            elementTextByID("was-due-on-0")("Was due on 1 July 2017"),
            elementTextByID("submitted-on-date-0")("1 June 2017")
          )
        }

        "the user has multiple quarterly business income obligations only and no previous obligations" in {
          enable(ObligationsPage)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, multipleBusinessesResponse)
          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(singleObligationQuarterlyModel(testSelfEmploymentId),
            singleObligationQuarterlyModel(otherTestSelfEmploymentId))))
          IncomeTaxViewChangeStub.stubGetPreviousObligationsNotFound(testNino)


          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)
          verifyReportDeadlinesCall(testNino)
          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title, username and links")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays all the business obligation dates")
          res should have(
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(1) div:nth-child(2)")(expectedValue = "6 April 2017 to 5 July 2017"),
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(2) div:nth-child(2)")(expectedValue = "1 January 2018"),
            elementTextBySelector("#quarterly-return-section-1 div div:nth-child(1) div:nth-child(2)")(expectedValue = "6 April 2017 to 5 July 2017"),
            elementTextBySelector("#quarterly-return-section-1 div div:nth-child(2) div:nth-child(2)")(expectedValue = "1 January 2018"),
            isElementVisibleById("quarterly-return-section-2")(expectedValue = false)
          )

          Then("the page displays no previous obligations")
          res should have(
            elementTextByID("no-previous-obligations")(obligationsMessages.noPreviousObligations)
          )
        }

        "the user has multiple quarterly business income obligations and previous obligations" in {
          enable(ObligationsPage)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, multipleBusinessesResponse)
          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(singleObligationQuarterlyModel(testSelfEmploymentId),
            singleObligationQuarterlyModel(otherTestSelfEmploymentId))))
          IncomeTaxViewChangeStub.stubGetPreviousObligations(testNino, previousObligationsModel)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)
          verifyReportDeadlinesCall(testNino)
          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title, username and links")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays all the business obligation dates")
          res should have(
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(1) div:nth-child(2)")(expectedValue = "6 April 2017 to 5 July 2017"),
            elementTextBySelector("#quarterly-return-section-0 div div:nth-child(2) div:nth-child(2)")(expectedValue = "1 January 2018"),
            elementTextBySelector("#quarterly-return-section-1 div div:nth-child(1) div:nth-child(2)")(expectedValue = "6 April 2017 to 5 July 2017"),
            elementTextBySelector("#quarterly-return-section-1 div div:nth-child(2) div:nth-child(2)")(expectedValue = "1 January 2018"),
            isElementVisibleById("quarterly-return-section-2")(expectedValue = false)
          )

          Then("the page displays the previous obligations")
          res should have(
            isElementVisibleById("no-previous-obligations")(expectedValue = false),
            elementTextByID("income-source-0")("Tax year - Final check"),
            elementTextByID("obligation-type-0")("Declaration"),
            elementTextByID("date-from-to-0")("1 May 2017 to 1 June 2017"),
            elementTextByID("was-due-on-0")("Was due on 1 July 2017"),
            elementTextByID("submitted-on-date-0")("1 June 2017")
          )
        }

        "the user has a eops SE income obligation only and no previous obligations" in {
          enable(ObligationsPage)
          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, singleBusinessResponse)
          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(SEIncomeSourceEOPSModel(testSelfEmploymentId))))
          IncomeTaxViewChangeStub.stubGetPreviousObligationsNotFound(testNino)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)
          verifyReportDeadlinesCall(testNino)
          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays SE income source obligation dates")
          res should have(
            elementTextBySelector("#eops-return-section-0 div div:nth-child(1) div:nth-child(2)")("6 April 2017 to 5 April 2018"),
            elementTextBySelector("#eops-return-section-0 div div:nth-child(2) div:nth-child(2)")("31 January 2018")
          )

          Then("the page displays no property obligation dates")
          res should have(
            isElementVisibleById("eops-return-section-1")(expectedValue = false)
          )

          Then("the page displays no previous obligations")
          res should have(
            elementTextByID("no-previous-obligations")(obligationsMessages.noPreviousObligations)
          )
        }

        "the user has a eops SE income obligation and previous obligations" in {
          enable(ObligationsPage)
          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, singleBusinessResponse)
          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(SEIncomeSourceEOPSModel(testSelfEmploymentId))))
          IncomeTaxViewChangeStub.stubGetPreviousObligations(testNino, previousObligationsModel)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)
          verifyReportDeadlinesCall(testNino)
          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays SE income source obligation dates")
          res should have(
            elementTextBySelector("#eops-return-section-0 div div:nth-child(1) div:nth-child(2)")("6 April 2017 to 5 April 2018"),
            elementTextBySelector("#eops-return-section-0 div div:nth-child(2) div:nth-child(2)")("31 January 2018")
          )

          Then("the page displays no property obligation dates")
          res should have(
            isElementVisibleById("eops-return-section-1")(expectedValue = false)
          )

          Then("the page displays the previous obligations")
          res should have(
            isElementVisibleById("no-previous-obligations")(expectedValue = false),
            elementTextByID("income-source-0")("Tax year - Final check"),
            elementTextByID("obligation-type-0")("Declaration"),
            elementTextByID("date-from-to-0")("1 May 2017 to 1 June 2017"),
            elementTextByID("was-due-on-0")("Was due on 1 July 2017"),
            elementTextByID("submitted-on-date-0")("1 June 2017")
          )
        }

        "the user has a Crystallised obligation only and no previous obligations" in {
          enable(ObligationsPage)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, singleBusinessResponse)
          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(noObligationsModel(testSelfEmploymentId), crystallisedEOPSModel)))
          IncomeTaxViewChangeStub.stubGetPreviousObligationsNotFound(testNino)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)
          verifyReportDeadlinesCall(testNino)
          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays crystallised obligation information")
          res should have(
            elementTextBySelector("#crystallised-section-0 div div:nth-child(1) div:nth-child(1)")(expectedValue = "Whole tax year (final check)"),
            elementTextBySelector("#crystallised-section-0 div div:nth-child(2) div:nth-child(1)")(expectedValue = "Due on:"),
            elementTextBySelector("#crystallised-section-0 div div:nth-child(1) div:nth-child(2)")("6 April 2017 to 5 April 2018"),
            elementTextBySelector("#crystallised-section-0 div div:nth-child(2) div:nth-child(2)")("31 January 2019")
          )

          Then("the page displays no property or business obligation dates")
          res should have(
            isElementVisibleById("eops-return-section-0")(expectedValue = false),
            isElementVisibleById("quarterly-return-section-0")(expectedValue = false)
          )

          Then("the page displays no previous obligations")
          res should have(
            elementTextByID("no-previous-obligations")(obligationsMessages.noPreviousObligations)
          )
        }

        "the user has a Crystallised obligation and previous obligations" in {
          enable(ObligationsPage)

          IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, singleBusinessResponse)
          IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(noObligationsModel(testSelfEmploymentId), crystallisedEOPSModel)))
          IncomeTaxViewChangeStub.stubGetPreviousObligations(testNino, previousObligationsModel)

          val res = IncomeTaxViewChangeFrontend.getReportDeadlines

          verifyIncomeSourceDetailsCall(testMtditid)
          verifyReportDeadlinesCall(testNino)
          IncomeTaxViewChangeStub.verifyGetPreviousObligations(testNino)

          Then("the view displays the correct title")
          res should have(
            httpStatus(OK),
            pageTitle(obligationsMessages.obligationsTitle)
          )

          Then("the page displays crystallised obligation information")
          res should have(
            elementTextBySelector("#crystallised-section-0 div div:nth-child(1) div:nth-child(1)")(expectedValue = "Whole tax year (final check)"),
            elementTextBySelector("#crystallised-section-0 div div:nth-child(2) div:nth-child(1)")(expectedValue = "Due on:"),
            elementTextBySelector("#crystallised-section-0 div div:nth-child(1) div:nth-child(2)")("6 April 2017 to 5 April 2018"),
            elementTextBySelector("#crystallised-section-0 div div:nth-child(2) div:nth-child(2)")("31 January 2019")
          )

          Then("the page displays no property or business obligation dates")
          res should have(
            isElementVisibleById("eops-return-section-0")(expectedValue = false),
            isElementVisibleById("quarterly-return-section-0")(expectedValue = false)
          )

          Then("the page displays the previous obligations")
          res should have(
            isElementVisibleById("no-previous-obligations")(expectedValue = false),
            elementTextByID("income-source-0")("Tax year - Final check"),
            elementTextByID("obligation-type-0")("Declaration"),
            elementTextByID("date-from-to-0")("1 May 2017 to 1 June 2017"),
            elementTextByID("was-due-on-0")("Was due on 1 July 2017"),
            elementTextByID("submitted-on-date-0")("1 June 2017")
          )
        }
      }
    }

    "the ReportDeadlines Feature is disabled" should {

      "Redirect to the Income Tax View Change Home Page" in {


        disable(ReportDeadlines)

        And("I wiremock stub a successful Income Source Details response with 1 Business and Property income")
        IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, businessAndPropertyResponse)

        And("I wiremock stub a single business obligation response")
        IncomeTaxViewChangeStub.stubGetReportDeadlines(testNino, ObligationsModel(Seq(singleObligationOverdueModel(testSelfEmploymentId))))

        When("I call GET /report-quarterly/income-and-expenses/view/obligations")
        val res = IncomeTaxViewChangeFrontend.getReportDeadlines

        verifyIncomeSourceDetailsCall(testMtditid)
        verifyReportDeadlinesCall(testNino)

        Then("the result should have a HTTP status of SEE_OTHER (303) and redirect to the Income Tax home page")
        res should have(
          httpStatus(SEE_OTHER),
          redirectURI(controllers.routes.HomeController.home().url)
        )
      }

    }

  }
}
