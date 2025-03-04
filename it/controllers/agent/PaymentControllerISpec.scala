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

package controllers.agent

import java.time.LocalDate

import assets.BaseIntegrationTestConstants._
import config.featureswitch.AgentViewer
import controllers.agent.utils.SessionKeys
import helpers.agent.ComponentSpecBase
import helpers.servicemocks.IncomeTaxViewChangeStub
import models.core.{AccountingPeriodModel, PaymentJourneyModel}
import models.incomeSourceDetails.{BusinessDetailsModel, IncomeSourceDetailsModel}
import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}

class PaymentControllerISpec extends ComponentSpecBase {

  val url: String = "/pay-api/mtd-income-tax/sa/journey/start"

  val getCurrentTaxYearEnd: LocalDate = {
    val currentDate: LocalDate = LocalDate.now
    if (currentDate.isBefore(LocalDate.of(currentDate.getYear, 4, 6))) LocalDate.of(currentDate.getYear, 4, 5)
    else LocalDate.of(currentDate.getYear + 1, 4, 5)
  }

  val clientDetails: Map[String, String] = Map(
    SessionKeys.clientFirstName -> "Test",
    SessionKeys.clientLastName -> "User",
    SessionKeys.clientUTR -> "1234567890",
    SessionKeys.clientNino -> testNino,
    SessionKeys.clientMTDID -> testMtditid,
    SessionKeys.confirmedClient -> "true"
  )

  val submissionJson: JsValue = Json.parse(
    s"""
       |{
       | "utr": "1234567890",
       | "amountInPence": 10000,
       | "returnUrl": "${appConfig.agentPaymentRedirectUrl}",
       | "backUrl": "${appConfig.agentPaymentRedirectUrl}"
       |}
    """.stripMargin
  )

  "Calling .paymentHandoff" should {

    "redirect the user correctly" when {

      "the payments api responds with a 200 and valid json" in {
        enable(AgentViewer)
        stubAuthorisedAgentUser(authorised = true)

        IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(
          status = OK,
          response = IncomeSourceDetailsModel(
            mtdbsa = testMtditid,
            yearOfMigration = None,
            businesses = List(BusinessDetailsModel(
              "testId",
              AccountingPeriodModel(LocalDate.now, LocalDate.now.plusYears(1)),
              None, None, None, None, None, None, None, None,
              Some(getCurrentTaxYearEnd)
            )),
            property = None
          )
        )

        IncomeTaxViewChangeStub.stubPayApiResponse(url, 201, Json.toJson(PaymentJourneyModel("id", "redirect-url")))

        val res = IncomeTaxViewChangeFrontend.getPay(10000, clientDetails)


        res.status shouldBe 303
        res.header("Location") shouldBe Some("redirect-url")
        IncomeTaxViewChangeStub.verifyStubPayApi(url, submissionJson)
      }
    }

    "return an internal server error" when {
      "the payments api responds with a 500" in {
        enable(AgentViewer)
        stubAuthorisedAgentUser(authorised = true)

        IncomeTaxViewChangeStub.stubGetIncomeSourceDetailsResponse(testMtditid)(
          status = OK,
          response = IncomeSourceDetailsModel(
            mtdbsa = testMtditid,
            yearOfMigration = None,
            businesses = List(BusinessDetailsModel(
              "testId",
              AccountingPeriodModel(LocalDate.now, LocalDate.now.plusYears(1)),
              None, None, None, None, None, None, None, None,
              Some(getCurrentTaxYearEnd)
            )),
            property = None
          )
        )

        IncomeTaxViewChangeStub.stubPayApiResponse(url, 500, Json.toJson(PaymentJourneyModel("id", "redirect-url")))


        val res = IncomeTaxViewChangeFrontend.getPay(10000, clientDetails)


        res.status shouldBe 500
        IncomeTaxViewChangeStub.verifyStubPayApi(url, submissionJson)
      }
    }
  }
}
