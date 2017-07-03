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

package assets

import auth.MtdItUser
import models._
import play.api.http.Status
import play.api.libs.json.Json
import utils.ImplicitDateFormatter

object TestConstants extends ImplicitDateFormatter {

  val testMtditid = "XAIT0000123456"
  val testNino = "AB123456C"
  val testMtdItUser: MtdItUser = MtdItUser(testMtditid, testNino)
  val testSelfEmploymentId = "XA00001234"
  val testTaxCalculationId = "CALCID"
  val testErrorStatus = Status.INTERNAL_SERVER_ERROR
  val testErrorMessage = "Dummy Error Message"

  object BusinessDetails {

    val business1 = BusinessModel(
      id = testSelfEmploymentId,
      accountingPeriod = AccountingPeriodModel(start = "2017-1-1", end = "2017-12-31"),
      accountingType = "CASH",
      commencementDate = Some("2017-1-1"),
      cessationDate = Some("2017-12-31"),
      tradingName = "business",
      businessDescription = Some("a business"),
      businessAddressLineOne = Some("64 Zoo Lane"),
      businessAddressLineTwo = Some("Happy Place"),
      businessAddressLineThree = Some("Magical Land"),
      businessAddressLineFour = Some("England"),
      businessPostcode = Some("ZL1 064")
    )
    val business2 = BusinessModel(
      id = "5678",
      accountingPeriod = AccountingPeriodModel(start = "2017-1-1", end = "2017-12-31"),
      accountingType = "CASH",
      commencementDate = Some("2017-1-1"),
      cessationDate = Some("2017-12-31"),
      tradingName = "otherBusiness",
      businessDescription = Some("some business"),
      businessAddressLineOne = Some("65 Zoo Lane"),
      businessAddressLineTwo = Some("Happy Place"),
      businessAddressLineThree = Some("Magical Land"),
      businessAddressLineFour = Some("England"),
      businessPostcode = Some("ZL1 064")
    )

    val businessesSuccessResponse = List(business1, business2)
    val businessesSuccessModel = BusinessDetailsModel(businessesSuccessResponse)
    val businessSuccessEmptyModel = BusinessDetailsModel(List.empty)
    val businessSuccessString =
      s"""
          {
             "business":[
                {
                   "id":"$testSelfEmploymentId",
                   "accountingPeriod":{
                      "start":"2017-01-01",
                      "end":"2017-12-31"
                   },
                   "accountingType":"CASH",
                   "commencementDate":"2017-01-01",
                   "cessationDate":"2017-12-31",
                   "tradingName":"business",
                   "businessDescription":"a business",
                   "businessAddressLineOne":"64 Zoo Lane",
                   "businessAddressLineTwo":"Happy Place",
                   "businessAddressLineThree":"Magical Land",
                   "businessAddressLineFour":"England",
                   "businessPostcode":"ZL1 064"
                },
                {
                   "id":"5678",
                   "accountingPeriod":{
                      "start":"2017-01-01",
                      "end":"2017-12-31"
                   },
                   "accountingType":"CASH",
                   "commencementDate":"2017-01-01",
                   "cessationDate":"2017-12-31",
                   "tradingName":"otherBusiness",
                   "businessDescription":"some business",
                   "businessAddressLineOne":"65 Zoo Lane",
                   "businessAddressLineTwo":"Happy Place",
                   "businessAddressLineThree":"Magical Land",
                   "businessAddressLineFour":"England",
                   "businessPostcode":"ZL1 064"
                }
             ]
          }
      """.stripMargin
    val businessSuccessJson = Json.parse(businessSuccessString)


    val businessErrorModel = BusinessDetailsErrorModel(testErrorStatus, testErrorMessage)
    val businessErrorString =
      s"""
        |{
        |  "code":$testErrorStatus,
        |  "message":"$testErrorMessage"
        |}
      """.stripMargin
    val businessListErrorJson = Json.parse(businessErrorString)
  }

  object Estimates {

    val testYear = 2018
    val testCalcType = "it"

    val lastTaxCalcSuccess = LastTaxCalculation(
      calcID = "01234567",
      calcTimestamp = "2017-07-06T12:34:56.789Z",
      calcAmount = 543.21
    )

    val lastTaxCalcError = LastTaxCalculationError(testErrorStatus, testErrorMessage)
  }

  object Obligations {

    def fakeObligationsModel(m: ObligationModel): ObligationModel = new ObligationModel(m.start,m.end,m.due,m.met) {
      override def currentTime() = "2017-10-31"
    }

    val receivedObligation = fakeObligationsModel(ObligationModel(
      start = "2017-04-01",
      end = "2017-6-30",
      due = "2017-7-31",
      met = true
    ))

    val overdueObligation = fakeObligationsModel(ObligationModel(
      start = "2017-7-1",
      end = "2017-9-30",
      due = "2017-10-30",
      met = false
    ))

    val openObligation = fakeObligationsModel(ObligationModel(
      start = "2017-7-1",
      end = "2017-9-30",
      due = "2017-10-31",
      met = false
    ))

    val obligationsDataSuccessModel = ObligationsModel(List(receivedObligation, overdueObligation, openObligation))
    val obligationsDataSuccessString =
      """
        |{
        |  "obligations": [
        |    {
        |      "start": "2017-04-01",
        |      "end": "2017-06-30",
        |      "due": "2017-07-31",
        |      "met": true
        |    },
        |    {
        |      "start": "2017-07-01",
        |      "end": "2017-09-30",
        |      "due": "2017-10-30",
        |      "met": false
        |    },
        |    {
        |      "start": "2017-07-01",
        |      "end": "2017-09-30",
        |      "due": "2017-10-31",
        |      "met": false
        |    }
        |  ]
        |}
      """.stripMargin
    val obligationsDataSuccessJson = Json.parse(obligationsDataSuccessString)

    val obligationsDataErrorModel = ObligationsErrorModel(testErrorStatus, testErrorMessage)
    val obligationsDataErrorString =
      s"""
        |{
        |  "code":$testErrorStatus,
        |  "message":"$testErrorMessage"
        |}
      """.stripMargin
    val obligationsDataErrorJson = Json.parse(obligationsDataErrorString)

  }

  object CalcBreakdown {
    val calculationDataSuccessModel = CalculationDataModel(incomeTaxYTD = Some(90500),
      incomeTaxThisPeriod = Some(2000),
      profitFromSelfEmployment = Some(200000),
      profitFromUkLandAndProperty = Some(10000),
      totalIncomeReceived = Some(230000),
      personalAllowance = Some(11500),
      totalIncomeOnWhichTaxIsDue = Some(198500),
      payPensionsProfitAtBRT = Some(20000),
      incomeTaxOnPayPensionsProfitAtBRT = Some(4000),
      payPensionsProfitAtHRT = Some(100000),
      incomeTaxOnPayPensionsProfitAtHRT = Some(40000),
      payPensionsProfitAtART = Some(50000),
      incomeTaxOnPayPensionsProfitAtART = Some(22500),
      incomeTaxDue = Some(66500),
      nicTotal = Some(24000),
      rateBRT = Some(20),
      rateHRT = Some(40),
      rateART = Some(45)
    )
    val calculationDataSuccessString =
      """
        |{
        | "incomeTaxYTD": 90500,
        | "incomeTaxThisPeriod": 2000,
        | "profitFromSelfEmployment": 200000,
        | "profitFromUkLandAndProperty": 10000,
        | "totalIncomeReceived": 230000,
        | "personalAllowance": 11500,
        | "totalIncomeOnWhichTaxIsDue": 198500,
        | "payPensionsProfitAtBRT": 20000,
        | "incomeTaxOnPayPensionsProfitAtBRT": 4000,
        | "payPensionsProfitAtHRT": 100000,
        | "incomeTaxOnPayPensionsProfitAtHRT": 40000,
        | "payPensionsProfitAtART": 50000,
        | "incomeTaxOnPayPensionsProfitAtART": 22500,
        | "incomeTaxDue": 66500,
        | "nicTotal": 24000,
        | "rateBRT": 20,
        | "rateHRT": 40,
        | "rateART": 45
        |}
      """.stripMargin
    val calculationDataSuccessJson = Json.parse(calculationDataSuccessString)

    val calculationDataErrorModel = CalculationDataErrorModel(testErrorStatus, testErrorMessage)
    val calculationDataErrorString =
      s"""
         |{
         |  "code":$testErrorStatus,
         |  "message":"$testErrorMessage"
         |}
       """.stripMargin
    val calculationDataErrorJson = Json.parse(calculationDataErrorString)
  }
}
