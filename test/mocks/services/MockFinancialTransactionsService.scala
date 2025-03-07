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

package mocks.services

import java.time.LocalDate

import assets.BaseTestConstants.{testMtditid, testTaxYear}
import assets.FinancialTransactionsTestConstants.{financialTransactionsErrorModel, financialTransactionsModel}
import models.financialTransactions.FinancialTransactionsResponseModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import services.FinancialTransactionsService
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.Future

trait MockFinancialTransactionsService extends UnitSpec with MockitoSugar with BeforeAndAfterEach {

  val mockFinancialTransactionsService: FinancialTransactionsService = mock[FinancialTransactionsService]


  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockFinancialTransactionsService)
  }

  def setupMockGetFinancialTransactions(mtditid: String, taxYear: Int)(response: FinancialTransactionsResponseModel): Unit =
    when(mockFinancialTransactionsService.getFinancialTransactions(
      ArgumentMatchers.eq(mtditid), ArgumentMatchers.eq(taxYear)
    )(ArgumentMatchers.any())).thenReturn(Future.successful(response))

  def mockFinancialTransactionSuccess(taxYear: LocalDate = LocalDate.of(2018,4,5)): Unit =
    setupMockGetFinancialTransactions(testMtditid, testTaxYear)(financialTransactionsModel(taxYear))

  def mockFinancialTransactionFailed(): Unit =
    setupMockGetFinancialTransactions(testMtditid, testTaxYear)(financialTransactionsErrorModel)

  def mockGetAllFinancialTransactions(response: List[(Int, FinancialTransactionsResponseModel)]) = {
    when(mockFinancialTransactionsService.getAllFinancialTransactions(ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.any()))
      .thenReturn(Future.successful(response))
  }
}
