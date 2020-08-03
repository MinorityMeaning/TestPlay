import controllers.HomeController
import org.scalatestplus.play.PlaySpec
import play.api.test.FakeRequest
import play.api.test.Helpers
import play.api.test.Helpers._

class ControllerSpec extends PlaySpec {
  "HomeController#index" must {
    "Вернуть ожидаемую страницу" in {
      val controller = new HomeController(Helpers.stubControllerComponents())
      val result = controller.index.apply(FakeRequest())
      val bodyText = contentAsString(result)
      bodyText must include ("Welcome to Play")
      bodyText must include ("Congratulations, you’ve just created")
      bodyText must include ("Why do you see this page?")
    }
  }
}
