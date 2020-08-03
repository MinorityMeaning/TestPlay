package controllers

import javax.inject._
import models.HomeInMemoryModel
import play.api.mvc._
import play.api.data._
import play.api.data.Forms._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */

case class LoginData(username: String, password: String)

@Singleton
class HomeController @Inject()(cc: MessagesControllerComponents) (implicit assetsFinder: AssetsFinder)
  extends MessagesAbstractController(cc) {

  /**
   * Create an Action to render an HTML page with a welcome message.
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */

  val loginForm = Form(mapping(
    "Username" -> text(3, 10),
    "Password" -> text(8)
  )(LoginData.apply)(LoginData.unapply)
  )

  def index = Action { implicit request =>
    Ok(views.html.index("Your new application is ready."))
  }

  def login = Action { implicit request =>
    Ok(views.html.login(loginForm))
  }

  def TaskList = Action { implicit request =>
    val usernameOption = request.session.get("username")
    usernameOption.map { username =>
      val tasks = HomeInMemoryModel.getTasks(username)
      Ok(views.html.taskList(tasks))
    }.getOrElse(Redirect(routes.HomeController.login()))

  }

  def validateLoginPost = Action {implicit request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map { args =>
      val username = args("username").head
      val password = args("password").head
      if (HomeInMemoryModel.validateUser(username, password)) {
        Redirect(routes.HomeController.TaskList()).withSession("username" -> username)
      } else {
        Redirect(routes.HomeController.login()).flashing("error" -> "Неверный логин или пароль.")
      }
    }.getOrElse(Redirect(routes.HomeController.login()))
  }

  def validateLoginForm = Action { implicit request =>
    loginForm.bindFromRequest.fold(
        formWithErrors => BadRequest(views.html.login(formWithErrors)),
        ld =>
          if (HomeInMemoryModel.validateUser(ld.username, ld.password)) {
            Redirect(routes.HomeController.TaskList()).withSession("username" -> ld.username)
          } else {
            Redirect(routes.HomeController.login()).flashing("error" -> "Неверный логин или пароль.")
          }
    )
  }

  def createUserPost = Action { implicit request =>
    val postVals = request.body.asFormUrlEncoded
    postVals.map { args =>
      val username = args("username").head
      val password = args("password").head
      if (HomeInMemoryModel.createUser(username, password)) {
        Redirect(routes.HomeController.login()).flashing("success" -> "Пользователь успешно создан")
      } else {
        Redirect(routes.HomeController.login()).flashing("error" -> "Ошибка создания пользователя")
      }
    }.getOrElse(Redirect(routes.HomeController.login()))

  }

  def logout = Action{
    Redirect(routes.HomeController.login()).withNewSession
  }

  def addTask = Action {implicit request =>
    val usernameOption = request.session.get("username")
    usernameOption.map { username =>
      val postVals = request.body.asFormUrlEncoded
      postVals.map { args =>
        val task = args("newTask").head
        HomeInMemoryModel.addTask(username, task)
        Redirect(routes.HomeController.TaskList())
      }.getOrElse(Redirect(routes.HomeController.TaskList()))
    }.getOrElse(Redirect(routes.HomeController.login()))
  }

  def deleteTask = Action { implicit request =>
    val usernameOption = request.session.get("username")
    usernameOption.map { username =>
      val postVals = request.body.asFormUrlEncoded
      postVals.map { args =>
        val index = args("index").head.toInt
        HomeInMemoryModel.removeTask(username, index)
        Redirect(routes.HomeController.TaskList())
      }.getOrElse(Redirect(routes.HomeController.TaskList()))
    }.getOrElse(Redirect(routes.HomeController.login()))
  }


}
