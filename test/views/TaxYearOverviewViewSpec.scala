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

package views

import java.time.LocalDate

import assets.FinancialDetailsTestConstants.fullChargeModel
import assets.MessagesLookUp.Breadcrumbs
import assets.ReportDeadlinesTestConstants._
import implicits.ImplicitCurrencyFormatter.CurrencyFormatter
import implicits.ImplicitDateFormatterImpl
import models.calculation.CalcOverview
import models.financialDetails.Charge
import models.financialTransactions.TransactionModel
import models.reportDeadlines.{ObligationsModel, ReportDeadlineModelWithIncomeType}
import org.jsoup.nodes.Element
import play.twirl.api.Html
import testUtils.ViewSpec
import views.html.taxYearOverview

class TaxYearOverviewViewSpec extends ViewSpec {

  val testYear: Int = 2018


  val implicitDateFormatter: ImplicitDateFormatterImpl = app.injector.instanceOf[ImplicitDateFormatterImpl]

  import implicitDateFormatter._

  def completeOverview(crystallised: Boolean): CalcOverview = CalcOverview(
    timestamp = Some("2020-01-01T00:35:34.185Z"),
    income = 1.01,
    deductions = 2.02,
    totalTaxableIncome = 3.03,
    taxDue = 4.04,
    payment = 5.05,
    totalRemainingDue = 6.06,
    crystallised = crystallised
  )

  val transactionModel: TransactionModel = TransactionModel(
    clearedAmount = Some(7.07),
    outstandingAmount = Some(8.08)
  )

  val testChargesList: List[Charge] = List(fullChargeModel)
  val emptyChargeList: List[Charge] = List.empty

  val testObligationsModel: ObligationsModel = ObligationsModel(Seq(reportDeadlinesDataSelfEmploymentSuccessModel))

  def estimateView(chargeList: List[Charge] = testChargesList, obligations: ObligationsModel = testObligationsModel): Html = taxYearOverview(
    testYear, completeOverview(false), chargeList, obligations, mockImplicitDateFormatter, "testBackURL")

  def crystallisedView: Html = taxYearOverview(testYear, completeOverview(true), testChargesList, testObligationsModel, mockImplicitDateFormatter, "testBackURL")

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.by(_.toEpochDay)

  object taxYearOverviewMessages {
    val title: String = "Tax year overview - Business Tax account - GOV.UK"
    val heading: String = "Tax year overview"
    val secondaryHeading: String = s"6 April ${testYear - 1} to 5 April $testYear"
    val calculationDate: String = "Calculation date"
    val calcDate: String = "1 January 2020"
    val estimate: String = s"6 April ${testYear - 1} to 1 January 2020 estimate"
    val totalDue: String = "Total Due"
    val taxDue: String = "£4.04"
    val calcDateInfo: String = "This calculation is from the last time you viewed your tax calculation in your own software. You will need to view it in your software for the most up to date version."
    val taxCalculation: String = "Tax calculation"
    val payments: String = "Payments"
    val updates: String = "Updates"
    val income: String = "Income"
    val allowancesAndDeductions: String = "Allowances and deductions"
    val totalIncomeDue: String = "Total income on which tax is due"
    val incomeTaxNationalInsuranceDue: String = "Income Tax and National Insurance contributions due"
    val paymentType: String = "Payment type"
    val dueDate: String = "Due date"
    val status: String = "Status"
    val amount: String = "Amount"
    val paymentOnAccount1: String = "Payment on account 1 of 2"
    val unpaid: String = "Unpaid"
    val partPaid: String = "Part Paid"
    val noPaymentsDue: String = "No payments currently due."
    val updateType: String = "Update type"
    val updateIncomeSource: String = "Income source"
    val updateDateSubmitted: String = "Date submitted"

    def updateCaption(from: String, to: String): String = s"$from to $to"

    def dueMessage(due: String): String = s"Due $due"

    def incomeType(incomeType: String) = {
      incomeType match {
        case "Property" => "Property income"
        case "Business" => "Business"
        case "Crystallised" => "All income sources"
        case other => other
      }
    }

    def updateType(updateType: String) = {
      updateType match {
        case "Quarterly" => "Quarterly Update"
        case "EOPS" => "Annual Update"
        case "Crystallised" => "Final Declaration"
        case _ => updateType
      }
    }
  }

  "taxYearOverview" should {
    "have the correct title" in new Setup(estimateView()) {
      document.title shouldBe taxYearOverviewMessages.title
    }

    "have the correct heading" in new Setup(estimateView()) {
      content.selectHead("  h1").text shouldBe taxYearOverviewMessages.heading
    }

    "have the correct secondary heading" in new Setup(estimateView()) {
      content.selectHead("header > p").text shouldBe taxYearOverviewMessages.secondaryHeading
    }

    "display the calculation date" in new Setup(estimateView()) {
      content.selectHead("dl > div:nth-child(1) > dd:nth-child(1)").text shouldBe taxYearOverviewMessages.calculationDate
      content.selectHead("dl > div:nth-child(1) > dd:nth-child(2)").text shouldBe taxYearOverviewMessages.calcDate
    }

    "display the estimate due for an ongoing tax year" in new Setup(estimateView()) {
      content.selectHead("dl > div:nth-child(2) > dd:nth-child(1)").text shouldBe taxYearOverviewMessages.estimate
      content.selectHead("dl > div:nth-child(2) > dd:nth-child(2)").text shouldBe completeOverview(false).taxDue.toCurrencyString
    }

    "display the total due for a crystallised year" in new Setup(crystallisedView) {
      content.selectHead("dl > div:nth-child(2) > dd:nth-child(1)").text shouldBe taxYearOverviewMessages.totalDue
      content.selectHead("dl > div:nth-child(2) > dd:nth-child(2)").text shouldBe completeOverview(true).taxDue.toCurrencyString
    }

    "have a paragraph explaining the calc date for an ongoing year" in new Setup(estimateView()) {
      content.selectHead(".panel").text shouldBe taxYearOverviewMessages.calcDateInfo
    }

    "not have a paragraph explaining the calc date for a crystallised year" in new Setup(crystallisedView) {
      content.getOptionalSelector(".panel") shouldBe None
    }

    "show three tabs with the correct tab headings" in new Setup(estimateView()) {
      content.selectHead("#tab_taxCalculation").text shouldBe taxYearOverviewMessages.taxCalculation
      content.selectHead("#tab_payments").text shouldBe taxYearOverviewMessages.payments
      content.selectHead("#tab_updates").text shouldBe taxYearOverviewMessages.updates
    }

    "when in an ongoing year should display the correct heading in the Tax Calculation tab" in new Setup(estimateView()) {
      content.selectHead("#taxCalculation > h2").text shouldBe taxYearOverviewMessages.estimate
    }

    "when in a crystallised year should display the correct heading in the Tax Calculation tab" in new Setup(crystallisedView) {
      content.selectHead("#taxCalculation > h2").text shouldBe taxYearOverviewMessages.taxCalculation
    }

    "display the income row in the Tax Calculation tab" in new Setup(estimateView()) {
      val incomeLink: Element = content.selectHead(" #income-deductions-table tr:nth-child(1) td:nth-child(1) a")
      incomeLink.text shouldBe taxYearOverviewMessages.income
      incomeLink.attr("href") shouldBe controllers.routes.IncomeSummaryController.showIncomeSummary(testYear).url
      content.selectHead("#income-deductions-table tr:nth-child(1) .numeric").text shouldBe completeOverview(false).income.toCurrencyString
    }

    "display the Allowances and deductions row in the Tax Calculation tab" in new Setup(estimateView()) {
      val allowancesLink: Element = content.selectHead(" #income-deductions-table tr:nth-child(2) td:nth-child(1) a")
      allowancesLink.text shouldBe taxYearOverviewMessages.allowancesAndDeductions
      allowancesLink.attr("href") shouldBe controllers.routes.DeductionsSummaryController.showDeductionsSummary(testYear).url
      content.selectHead("#income-deductions-table tr:nth-child(2) .numeric").text shouldBe completeOverview(false).deductions.toNegativeCurrencyString
    }

    "display the Total income on which tax is due row in the Tax Calculation tab" in new Setup(estimateView()) {
      content.selectHead(".total-section:nth-child(1)").text shouldBe taxYearOverviewMessages.totalIncomeDue
      content.selectHead(".total-section:nth-child(2)").text shouldBe completeOverview(false).totalTaxableIncome.toCurrencyString
    }

    "display the Income Tax and National Insurance Contributions Due row in the Tax Calculation tab" in new Setup(estimateView()) {
      val totalTaxDueLink: Element = content.selectHead("#taxdue-payments-table td:nth-child(1) a")
      totalTaxDueLink.text shouldBe taxYearOverviewMessages.incomeTaxNationalInsuranceDue
      totalTaxDueLink.attr("href") shouldBe controllers.routes.TaxDueSummaryController.showTaxDueSummary(testYear).url
      content.selectHead("#taxdue-payments-table td:nth-child(2)").text shouldBe completeOverview(false).taxDue.toCurrencyString
    }

    "display the table headings in the Payments tab" in new Setup(estimateView()) {
      content.selectHead("#paymentTypeHeading").text shouldBe taxYearOverviewMessages.paymentType
      content.selectHead("#paymentDueDateHeading").text shouldBe taxYearOverviewMessages.dueDate
      content.selectHead("#paymentStatusHeading").text shouldBe taxYearOverviewMessages.status
      content.selectHead("#paymentAmountHeading").text shouldBe taxYearOverviewMessages.amount
    }

    "display the payment type as a link to Charge Summary in the Payments tab" in new Setup(estimateView()) {
      val paymentTypeLink: Element = content.selectHead("#payments-table tr:nth-child(2) td:nth-child(1) a")
      paymentTypeLink.text shouldBe taxYearOverviewMessages.paymentOnAccount1
      paymentTypeLink.attr("href") shouldBe controllers.routes.ChargeSummaryController.showChargeSummary(testYear, fullChargeModel.transactionId).url
    }

    "display the Due date in the Payments tab" in new Setup(estimateView()) {
      content.selectHead("#payments-table tr:nth-child(2) td:nth-child(2)").text shouldBe "15 May 2019"
    }

    "display the Status in the payments tab" in new Setup(estimateView()) {
      content.selectHead("#payments-table tr:nth-child(2) td:nth-child(3)").text shouldBe taxYearOverviewMessages.partPaid
    }

    "display the Amount in the payments tab" in new Setup(estimateView()) {
      content.selectHead("#payments-table tr:nth-child(2) td:nth-child(4)").text shouldBe "£1,400.00"
    }

    "display No payments due when there are no charges in the payments tab" in new Setup(estimateView(emptyChargeList)) {
      content.selectHead("#payments p").text shouldBe taxYearOverviewMessages.noPaymentsDue
    }

    "display updates by due-date" in new Setup(estimateView()) {

      testObligationsModel.allDeadlinesWithSource(previous = true).groupBy[LocalDate] { reportDeadlineWithIncomeType =>
        reportDeadlineWithIncomeType.obligation.due
      }.toList.sortBy(_._1)(localDateOrdering).reverse.map { case (due: LocalDate, obligations: Seq[ReportDeadlineModelWithIncomeType]) =>
        content.selectHead(s"#table-default-content-$due").text shouldBe taxYearOverviewMessages.dueMessage(due.toLongDate)
        val sectionContent = content.selectHead(s"#updates")
        obligations.zip(1 to obligations.length).foreach {
          case (testObligation, index) =>
            val divAccordion = sectionContent.selectHead(s"div:nth-of-type($index)")

            divAccordion.selectHead("caption").text shouldBe
              taxYearOverviewMessages.updateCaption(testObligation.obligation.start.toLongDate, testObligation.obligation.end.toLongDate)
            divAccordion.selectHead("thead").selectNth("th", 1).text shouldBe taxYearOverviewMessages.updateType
            divAccordion.selectHead("thead").selectNth("th", 2).text shouldBe taxYearOverviewMessages.updateIncomeSource
            divAccordion.selectHead("thead").selectNth("th", 3).text shouldBe taxYearOverviewMessages.updateDateSubmitted
            val row = divAccordion.selectHead("tbody").selectHead("tr")
            row.selectNth("td", 1).text shouldBe taxYearOverviewMessages.updateType(testObligation.obligation.obligationType)
            row.selectNth("td", 2).text shouldBe taxYearOverviewMessages.incomeType(testObligation.incomeType)
            row.selectNth("td", 3).text shouldBe testObligation.obligation.dateReceived.map(_.toLongDateShort).getOrElse("")
        }
      }
    }
  }
}
