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

package views.agent

import org.jsoup.select.Elements
import play.api.mvc.Call
import play.twirl.api.Html
import testUtils.ViewSpec
import views.html.agent.UTRError

class UTRErrorViewSpec extends ViewSpec {

  lazy val postAction: Call = controllers.agent.routes.UTRErrorController.submit()
  lazy val testClientUtr: String = "1234567890"

  val utrError: UTRError = app.injector.instanceOf[UTRError]

  val utrErrorView: Html = utrError(
    testClientUtr,
    postAction
  )

  object utrErrorMessages {
    val title: String = "There’s a problem - Your client’s Income Tax details - GOV.UK"
    val heading: String = "There’s a problem"
    val utrWrong: String = s"The UTR you have entered $testClientUtr may be wrong because:"
    val reasonBullet1: String = "you have entered it incorrectly"
    val reasonBullet2Link: String = "Making Tax Digital for Income Tax (opens in new tab)"
    val reasonBullet2: String = s"you or your client have not signed up to $reasonBullet2Link yet"
    val goBackButton: String = "Go back and enter a different UTR"
  }

  "The UTR Error page" should {

    s"have the title: ${utrErrorMessages.title}" in new Setup(utrErrorView) {
      document.title shouldBe utrErrorMessages.title
    }

    s"have the heading: ${utrErrorMessages.heading}" in new Setup(utrErrorView) {
      document hasPageHeading utrErrorMessages.heading
    }

    s"have a paragraph stating: ${utrErrorMessages.utrWrong}" in new Setup(utrErrorView) {
      content.select("article p").text shouldBe utrErrorMessages.utrWrong
    }

    s"have a first bullet point: ${utrErrorMessages.reasonBullet1}" in new Setup(utrErrorView) {
      content.select("ul li:nth-child(1)").text shouldBe utrErrorMessages.reasonBullet1
    }

    s"have a second bullet point: ${utrErrorMessages.reasonBullet2}" in new Setup(utrErrorView) {
      content.select("ul li:nth-child(2)").text shouldBe utrErrorMessages.reasonBullet2
    }

    s"have a link in the second bullet point: ${utrErrorMessages.reasonBullet2Link}" in new Setup(utrErrorView) {
      val link: Elements = content.select("ul li:nth-child(2) a")
      link.text shouldBe utrErrorMessages.reasonBullet2Link
      link.attr("href") shouldBe "https://www.gov.uk/government/collections/making-tax-digital-for-income-tax"
    }

    s"have a button stating: ${utrErrorMessages.goBackButton}" in new Setup(utrErrorView) {
      content.select("#continue-button").text shouldBe utrErrorMessages.goBackButton
    }
  }
}
