package web

import config.Roles
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import io.javalin.security.SecurityUtil.roles
import org.koin.standalone.KoinComponent
import web.controllers.*

class Router(private val diagramController: DiagramController,
             private val userController: UserController) : KoinComponent {
  fun register(app: Javalin) {
      val rolesOptionalAuthenticated = roles(Roles.ANYONE, Roles.AUTHENTICATED)
      app.routes {
          path("users") {
              post(userController::register, roles(Roles.ANYONE))
              post("login", userController::login, roles(Roles.ANYONE))
          }
      path("diagrams") {
          post(diagramController::create)
          get(diagramController::findById)
      }

      }
  }
}
