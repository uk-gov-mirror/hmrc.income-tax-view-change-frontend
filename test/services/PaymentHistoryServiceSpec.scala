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

package services

import java.time.LocalDate

import assets.BaseTestConstants.{testMtditid, testNino, testRetrievedUserName, testTaxYear}
import assets.IncomeSourceDetailsTestConstants.{businessAndPropertyAligned, oldUserDetails}
import auth.MtdItUser
import config.featureswitch.{API5, FeatureSwitching}
import mocks.connectors.MockIncomeTaxViewChangeConnector
import models.financialDetails.{Payment, Payments, PaymentsError, PaymentsResponse}
import play.api.libs.json.{JsObject, Json}
import play.api.test.FakeRequest
import services.PaymentHistoryService
import services.PaymentHistoryService.PaymentHistoryError
import testUtils.TestSupport

class PaymentHistoryServiceSpec  extends TestSupport with MockIncomeTaxViewChangeConnector with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(API5)
  }

  val getCurrentTaxEndYear: Int = {
    val currentDate = LocalDate.now
    if (currentDate.isBefore(LocalDate.of(currentDate.getYear, 4, 6))) currentDate.getYear
    else currentDate.getYear + 1

  }

  val paymentFull: Payment = Payment(
    reference = Some("reference"),
    amount = Some(100.00),
    method = Some("method"),
    lot = Some("lot"),
    lotItem = Some("lotItem"),
    date = Some("date")
  )

  val oldUser: MtdItUser[_] = MtdItUser(
    mtditid = testMtditid,
    nino = testNino,
    userName = Some(testRetrievedUserName),
    incomeSources = oldUserDetails,
    saUtr = Some("saUtr"),
    credId = Some("credId"),
    userType = Some("individual")
  )(FakeRequest())


  object TestPaymentHistoryService extends PaymentHistoryService(mockIncomeTaxViewChangeConnector, appConfig)

  "getPaymentHistory" when {
    "An error is returned from the connector" should {
      "return a payment history error" in {
        setupGetPayments(getCurrentTaxEndYear)(PaymentsError(500, "ERROR"))
        setupGetPayments(getCurrentTaxEndYear - 1)(Payments(List.empty))
        await(TestPaymentHistoryService.getPaymentHistory) shouldBe Left(PaymentHistoryError)

      }
    }

      "a successful Payment History response is returned from the connector" should {
            "return a list of payments and ignore any payment data not found (404s)" in {
              setupGetPayments(getCurrentTaxEndYear)(PaymentsError(404, "NOT FOUND"))
              setupGetPayments(getCurrentTaxEndYear - 1)(Payments(List(paymentFull)))
              await(TestPaymentHistoryService.getPaymentHistory) shouldBe Right(List(paymentFull))
            }

      }

  }

  }

