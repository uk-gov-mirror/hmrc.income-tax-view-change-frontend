/*
 * Copyright 2018 HM Revenue & Customs
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

package models

import assets.BaseTestConstants._
import assets.IncomeSourcesWithDeadlinesTestConstants._
import assets.BusinessDetailsTestConstants._
import assets.PropertyDetailsTestConstants._
import org.scalatest.Matchers
import uk.gov.hmrc.play.test.UnitSpec

class IncomeSourcesWithDeadlinesModelSpec extends UnitSpec with Matchers {

  "The IncomeSourceDetailsWithDeadlinesModel" when {
    "the user has both businesses and property income sources" should {
      //Test Business details
      s"have a business ID of $testSelfEmploymentId" in {
        businessAndPropertyIncomeWithDeadlines.businessIncomeSources.head.incomeSource.incomeSourceId shouldBe testSelfEmploymentId
      }
      s"have the businesses accounting period start date of ${testBusinessAccountingPeriod.start}" in {
        businessAndPropertyIncomeWithDeadlines.businessIncomeSources.head.incomeSource.accountingPeriod.start shouldBe testBusinessAccountingPeriod.start
      }
      s"have the businesses accounting period end date of ${testBusinessAccountingPeriod.end}" in {
        businessAndPropertyIncomeWithDeadlines.businessIncomeSources.head.incomeSource.accountingPeriod.end shouldBe testBusinessAccountingPeriod.end
      }
      s"should have the trading name of 'Test Business'" in {
        businessAndPropertyIncomeWithDeadlines.businessIncomeSources.head.incomeSource.tradingName.get shouldBe testTradeName
      }
      //Test Property details
      s"have the property accounting period start date of ${testPropertyAccountingPeriod.start}" in {
        businessAndPropertyIncomeWithDeadlines.propertyIncomeSource.get.incomeSource.accountingPeriod.start shouldBe testPropertyAccountingPeriod.start
      }
      s"have the property accounting period end date of ${testPropertyAccountingPeriod.end}" in {
        businessAndPropertyIncomeWithDeadlines.propertyIncomeSource.get.incomeSource.accountingPeriod.end shouldBe testPropertyAccountingPeriod.end
      }
    }

    "the user has just a business income source" should {
      s"have a business ID of $testSelfEmploymentId" in {
        businessIncomeSourceSuccess.businessIncomeSources.head.incomeSource.incomeSourceId shouldBe testSelfEmploymentId
      }
      s"have the businesses accounting period start date of ${testBusinessAccountingPeriod.start}" in {
        businessIncomeSourceSuccess.businessIncomeSources.head.incomeSource.accountingPeriod.start shouldBe testBusinessAccountingPeriod.start
      }
      s"have the businesses accounting period end date of ${testBusinessAccountingPeriod.end}" in {
        businessIncomeSourceSuccess.businessIncomeSources.head.incomeSource.accountingPeriod.end shouldBe testBusinessAccountingPeriod.end
      }
      s"should have the trading name of 'Test Business'" in {
        businessIncomeSourceSuccess.businessIncomeSources.head.incomeSource.tradingName.get shouldBe testTradeName
      }
      //Test Property details
      s"should not have property details" in {
        businessIncomeSourceSuccess.propertyIncomeSource shouldBe None
      }
    }
    "the user has just a property income source" should {
      //Test Property details
      s"have the property accounting period start date of ${testPropertyAccountingPeriod.start}" in {
        propertyIncomeSourceSuccess.propertyIncomeSource.get.incomeSource.accountingPeriod.start shouldBe testPropertyAccountingPeriod.start
      }
      s"have the property accounting period end date of ${testPropertyAccountingPeriod.end}" in {
        propertyIncomeSourceSuccess.propertyIncomeSource.get.incomeSource.accountingPeriod.end shouldBe testPropertyAccountingPeriod.end
      }
      //Test Business Details
      "should not have business details" in {
        propertyIncomeSourceSuccess.businessIncomeSources shouldBe List.empty
      }
    }
    "the user has no income source" should {
      "return None for both business and property sources" in {
        noIncomeSourceSuccess.propertyIncomeSource shouldBe None
        noIncomeSourceSuccess.businessIncomeSources shouldBe List.empty
      }
    }
  }

}
