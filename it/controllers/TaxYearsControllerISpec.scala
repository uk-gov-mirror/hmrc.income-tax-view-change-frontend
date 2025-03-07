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

import java.time.LocalDateTime

import assets.BaseIntegrationTestConstants._
import assets.CalcDataIntegrationTestConstants._
import assets.IncomeSourceIntegrationTestConstants._
import assets.messages.{MyTaxYearsMessages => messages}
import config.featureswitch.{API5, FeatureSwitching}
import helpers.ComponentSpecBase
import helpers.servicemocks._
import models.calculation.{CalculationItem, ListCalculationItems}
import play.api.http.Status._

class TaxYearsControllerISpec extends ComponentSpecBase with FeatureSwitching {

  "Calling the TaxYearsController.viewTaxYears" when {

    "Api5 is disabled" when {

      "isAuthorisedUser with an active enrolment and income source has retrieved successfully" when {

        "the get all latest calculations brings back an error" should {

          "return 500 INTERNAL_SERVER_ERROR " in {

            disable(API5)

            And("I wiremock stub a successful Income Source Details response with single Business and Property income")
            IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, businessAndPropertyResponse)

            And("I stub a get calculation list response brings back an error for 2017-18")
            IndividualCalculationStub.stubGetCalculationList(testNino, "2017-18")(
              status = INTERNAL_SERVER_ERROR,
              body = ListCalculationItems(Seq(CalculationItem("idOne", LocalDateTime.now())))
            )

            When(s"I call GET /report-quarterly/income-and-expenses/view/tax-years")
            val res = IncomeTaxViewChangeFrontend.getTaxYears

            verifyIncomeSourceDetailsCall(testMtditid)

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )
          }
        }

        "the get all latest calculations brings back a successful tax year" should {

          "return 200 OK " in {

            disable(API5)

            And("I wiremock stub a successful Income Source Details response with single Business and Property income")
            IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, businessAndPropertyResponse)

            And("I stub a successful get calculation list response for 2017-18")
            IndividualCalculationStub.stubGetCalculationList(testNino, "2017-18")(
              status = OK,
              body = ListCalculationItems(Seq(CalculationItem("idOne", LocalDateTime.now())))
            )

            IndividualCalculationStub.stubGetCalculation(testNino, "idOne")(
              status = OK,
              body = estimatedCalculationFullJson
            )

            When(s"I call GET /report-quarterly/income-and-expenses/view/tax-years")
            val res = IncomeTaxViewChangeFrontend.getTaxYears

            verifyIncomeSourceDetailsCall(testMtditid)
            IndividualCalculationStub.verifyGetCalculationList(testNino, "2017-18")
            IndividualCalculationStub.verifyGetCalculation(testNino, "idOne")


            Then("The view should have the correct headings and a single tax year display")
            res should have(
              httpStatus(OK),
              pageTitle(messages.taxYearsTitle),
              nElementsWithClass("govuk-summary-list__value")(1)
            )
          }
        }
      }
    }

    "Api5 is enabled" when {

      "isAuthorisedUser with an active enrolment and income source has retrieved successfully" when {

        "the get all latest calculations brings back an error" should {

          "return 500 INTERNAL_SERVER_ERROR " in {

            enable(API5)

            And("I wiremock stub a successful Income Source Details response with single Business and Property income")
            IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, businessAndPropertyResponse)

            And("I stub a get calculation list response brings back an error for 2017-18")
            IndividualCalculationStub.stubGetCalculationList(testNino, "2017-18")(
              status = INTERNAL_SERVER_ERROR,
              body = ListCalculationItems(Seq(CalculationItem("idOne", LocalDateTime.now())))
            )

            When(s"I call GET /report-quarterly/income-and-expenses/view/tax-years")
            val res = IncomeTaxViewChangeFrontend.getTaxYears

            verifyIncomeSourceDetailsCall(testMtditid)

            res should have(
              httpStatus(INTERNAL_SERVER_ERROR)
            )
          }
        }

        "the get all latest calculations brings back two successful tax years" should {

          "return 200 OK " in {

            enable(API5)

            And("I wiremock stub a successful Income Source Details response with single Business and Property income")
            IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(OK, multipleBusinessesAndPropertyResponse)

            And("I stub a successful get calculation response for 2017-18")
            IndividualCalculationStub.stubGetCalculationList(testNino, "2017-18")(
              status = OK,
              body = ListCalculationItems(Seq(CalculationItem("idOne", LocalDateTime.now())))
            )

            IndividualCalculationStub.stubGetCalculation(testNino, "idOne")(
              status = OK,
              body = estimatedCalculationFullJson
            )

            And("I stub a successful calculation response for 2018-19")
            IndividualCalculationStub.stubGetCalculationList(testNino, "2018-19")(
              status = OK,
              body = ListCalculationItems(Seq(CalculationItem("idTwo", LocalDateTime.now())))
            )
            IndividualCalculationStub.stubGetCalculation(testNino, "idTwo")(
              status = OK,
              body = estimatedCalculationFullJson
            )

            When(s"I call GET /report-quarterly/income-and-expenses/view/tax-years")
            val res = IncomeTaxViewChangeFrontend.getTaxYears

            verifyIncomeSourceDetailsCall(testMtditid)
            IndividualCalculationStub.verifyGetCalculationList(testNino, "2017-18")
            IndividualCalculationStub.verifyGetCalculation(testNino, "idOne")
            IndividualCalculationStub.verifyGetCalculationList(testNino, "2018-19")
            IndividualCalculationStub.verifyGetCalculation(testNino, "idTwo")

            Then("The view should have the correct headings and a single tax year display")
            res should have(
              httpStatus(OK),
              pageTitle(messages.taxYearsTitle),
              nElementsWithClass("govuk-summary-list__value")(2)
            )
          }
        }
      }
    }
  }

  unauthorisedTest("/tax-years")
}
