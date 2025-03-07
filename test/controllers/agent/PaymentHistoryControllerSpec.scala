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

package controllers.agent

import assets.BaseTestConstants.testAgentAuthRetrievalSuccess
import config.FrontendAppConfig
import config.featureswitch.{API5, AgentViewer, FeatureSwitching}
import implicits.{ImplicitDateFormatter, ImplicitDateFormatterImpl}
import mocks.MockItvcErrorHandler
import mocks.auth.MockFrontendAuthorisedFunctions
import mocks.services.MockIncomeSourceDetailsService
import models.financialDetails.Payment
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.mvc.{MessagesControllerComponents, Result}
import services.PaymentHistoryService
import services.PaymentHistoryService.PaymentHistoryError
import testUtils.TestSupport
import uk.gov.hmrc.http.InternalServerException
import uk.gov.hmrc.play.language.LanguageUtils

import scala.concurrent.{ExecutionContext, Future}

class PaymentHistoryControllerSpec  extends TestSupport
  with MockFrontendAuthorisedFunctions
  with MockIncomeSourceDetailsService
  with ImplicitDateFormatter
  with FeatureSwitching
  with MockItvcErrorHandler {

  override def beforeEach(): Unit = {
    super.beforeEach()
    enable(API5)
    enable(AgentViewer)
  }

  val testPayments: List[Payment] = List(
    Payment(Some("AAAAA"), Some(10000), Some("Payment"), Some("lot"), Some("lotitem"), Some("2019-12-25")),
    Payment(Some("BBBBB"), Some(5000), Some("tnemyap"), Some("lot"), Some("lotitem"), Some("2007-03-23"))
  )

  trait Setup {

    val paymentHistoryService: PaymentHistoryService = mock[PaymentHistoryService]

    val controller = new PaymentHistoryController(
      app.injector.instanceOf[views.html.agent.AgentsPaymentHistory],
      mockAuthService,
      mockIncomeSourceDetailsService,
      paymentHistoryService,
    )(app.injector.instanceOf[FrontendAppConfig],
      app.injector.instanceOf[LanguageUtils],
      app.injector.instanceOf[MessagesControllerComponents],
      app.injector.instanceOf[ImplicitDateFormatterImpl],
      app.injector.instanceOf[ExecutionContext],
      mockItvcErrorHandler)
  }


  "The PaymentHistoryControllerSpec.viewPaymentsHistory function" when {

    "obtaining a users payments - right" should {
      "send the user to the paymentsHistory page with data" in new Setup {
        enable(AgentViewer)
        setupMockAgentAuthRetrievalSuccess(testAgentAuthRetrievalSuccess)


        mockSingleBusinessIncomeSource()
        when(paymentHistoryService.getPaymentHistory(any(), any()))
          .thenReturn(Future.successful(Right(testPayments)))

        val result: Future[Result] = controller.viewPaymentHistory()(fakeRequestConfirmedClient())

        status(result) shouldBe Status.OK
      }

    }

        "Failing to retrieve a user's payments - left" should {
      "send the user to the internal service error page" in new Setup {
        enable(AgentViewer)
        setupMockAgentAuthRetrievalSuccess(testAgentAuthRetrievalSuccess)
        mockErrorIncomeSource()
        when(paymentHistoryService.getPaymentHistory(any(), any()))
          .thenReturn(Future.successful(Left(PaymentHistoryError)))

        val result: Future[Result] = controller.viewPaymentHistory()(fakeRequestConfirmedClient())

        intercept[InternalServerException](await(result))

      }

    }

        "Failing to retrieve income sources" should {
      "send the user to internal server error page" in new Setup {
        enable(AgentViewer)
        setupMockAgentAuthRetrievalSuccess(testAgentAuthRetrievalSuccess)
        mockErrorIncomeSource()

        val result: Future[Result] = controller.viewPaymentHistory()(fakeRequestConfirmedClient())

        intercept[InternalServerException](await(result))
      }
    }

      "User fails to be authorised" in new Setup {
        enable(AgentViewer)
        setupMockAgentAuthorisationException()

        val result = await(controller.viewPaymentHistory()(fakeRequestWithActiveSession))

        status(result) shouldBe Status.SEE_OTHER

      }
    }



}