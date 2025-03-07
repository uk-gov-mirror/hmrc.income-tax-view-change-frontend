
package controllers.agent

import config.featureswitch.{AgentViewer, FeatureSwitching}
import controllers.agent.utils.SessionKeys
import helpers.agent.ComponentSpecBase
import play.api.http.Status._
import play.api.libs.ws.WSResponse

class UTRErrorControllerISpec extends ComponentSpecBase with FeatureSwitching {

  override def beforeEach(): Unit = {
    super.beforeEach()
    disable(AgentViewer)
  }

  val clientUTR = Map(
    SessionKeys.clientUTR -> "1234567890"
  )

  s"GET ${controllers.agent.routes.UTRErrorController.show().url}" should {
    s"redirect ($SEE_OTHER) to ${controllers.routes.SignInController.signIn().url}" when {
      "the user is not authenticated" in {
        stubAuthorisedAgentUser(authorised = false)

        val result: WSResponse = IncomeTaxViewChangeFrontend.getUTRError()

        Then(s"The user is redirected to ${controllers.routes.SignInController.signIn().url}")
        result should have(
          httpStatus(SEE_OTHER),
          redirectURI(controllers.routes.SignInController.signIn().url)
        )
      }
    }

    s"return $OK with technical difficulties" when {
      "the user is authenticated but doesn't have the agent enrolment" in {
        stubAuthorisedAgentUser(authorised = true, hasAgentEnrolment = false)

        val result: WSResponse = IncomeTaxViewChangeFrontend.getUTRError()

        Then(s"Technical difficulties are shown with status OK")
        result should have(
          httpStatus(OK),
          pageTitle("Sorry, there is a problem with the service - Business Tax account - GOV.UK")
        )
      }
    }

    s"return $NOT_FOUND" when {
      "the agent viewer feature switch is disabled" in {
        stubAuthorisedAgentUser(authorised = true)

        val result: WSResponse = IncomeTaxViewChangeFrontend.getUTRError()

        Then("A not found page is returned to the user")
        result should have(
          httpStatus(NOT_FOUND),
          pageTitle("Page not found - 404 - Business Tax account - GOV.UK")
        )
      }
    }

    s"redirect ($SEE_OTHER) to ${controllers.agent.routes.EnterClientsUTRController.show().url}" when {
      "the agent viewer feature switch is enabled and the client's UTR is not in session" in {
        enable(AgentViewer)
        stubAuthorisedAgentUser(authorised = true)

        val result: WSResponse = IncomeTaxViewChangeFrontend.getUTRError()

        Then("The enter client's utr page is returned to the user")
        result should have (
          httpStatus(SEE_OTHER),
          redirectURI(controllers.agent.routes.EnterClientsUTRController.show().url)
        )
      }
    }

    s"return $OK with the UTR Error page" when {
      "the agent viewer feature switch is enabled and the client's UTR is in session" in {
        enable(AgentViewer)
        stubAuthorisedAgentUser(authorised = true)

        val result: WSResponse = IncomeTaxViewChangeFrontend.getUTRError(clientUTR)

        Then("The UTR Error page is returned to the user")
        result should have(
          httpStatus(OK),
          pageTitle("There’s a problem - Your client’s Income Tax details - GOV.UK")
        )
      }
    }
  }

  s"POST ${controllers.agent.routes.UTRErrorController.submit().url}" should {
    s"redirect ($SEE_OTHER) to ${controllers.routes.SignInController.signIn().url}" when {
      "the user is not authenticated" in {
        stubAuthorisedAgentUser(authorised = false)

        val result: WSResponse = IncomeTaxViewChangeFrontend.postUTRError

        Then(s"The user is redirected to ${controllers.routes.SignInController.signIn().url}")
        result should have(
          httpStatus(SEE_OTHER),
          redirectURI(controllers.routes.SignInController.signIn().url)
        )
      }
    }

    s"return $OK with technical difficulties" when {
      "the user is authenticated but doesn't have the agent enrolment" in {
        stubAuthorisedAgentUser(authorised = true, hasAgentEnrolment = false)

        val result: WSResponse = IncomeTaxViewChangeFrontend.postUTRError

        Then("Technical difficulties are shown with status OK")
        result should have(
          httpStatus(OK),
          pageTitle("Sorry, there is a problem with the service - Business Tax account - GOV.UK")
        )
      }
    }

    s"return $NOT_FOUND" when {
      "the agent viewer feature switch is disabled" in {
        stubAuthorisedAgentUser(authorised = true)

        val result: WSResponse = IncomeTaxViewChangeFrontend.postUTRError

        Then("A not found page is returned to the user")
        result should have(
          httpStatus(NOT_FOUND),
          pageTitle("Page not found - 404 - Business Tax account - GOV.UK")
        )
      }
    }

    s"redirect ($SEE_OTHER) to ${controllers.agent.routes.EnterClientsUTRController.show().url}" when {
      "the agent viewer feature switch is enabled" in {
        enable(AgentViewer)
        stubAuthorisedAgentUser(authorised = true)

        val result: WSResponse = IncomeTaxViewChangeFrontend.postUTRError

        Then(s"The user is redirected to ${controllers.agent.routes.EnterClientsUTRController.show().url}")
        result should have(
          httpStatus(SEE_OTHER),
          redirectURI(controllers.agent.routes.EnterClientsUTRController.show().url)
        )
      }
    }
  }

}
