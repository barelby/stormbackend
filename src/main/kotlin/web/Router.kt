package web

import config.Roles
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.security.SecurityUtil.roles
import org.koin.standalone.KoinComponent
import web.controllers.*

class Router(private val diagramController: DiagramController,
             private val userController: UserController,
             private val profileController: ProfileController,
             private val tagController: TagController) : KoinComponent {
  fun register(app: Javalin) {
      val rolesOptionalAuthenticated = roles(Roles.ANYONE, Roles.AUTHENTICATED)
      app.routes {
          path("users") {
              post(userController::register, roles(Roles.ANYONE))
              post("login", userController::login, roles(Roles.ANYONE))
          }
          path("user") {
              get(userController::getCurrent, roles(Roles.AUTHENTICATED))
              put(userController::update, roles(Roles.AUTHENTICATED))
          }
          path("profiles/:username") {
              get(profileController::get, rolesOptionalAuthenticated)
              path("follow") {
                  post(profileController::follow, roles(Roles.AUTHENTICATED))
                  delete(profileController::unfollow, roles(Roles.AUTHENTICATED))
              }
          }
          path("diagrams") {
          get("feed",diagramController::feed, roles(Roles.AUTHENTICATED))
          path(":id") {
              path("favorite") {
                  post(diagramController::favorite, roles(Roles.AUTHENTICATED))
                  delete(diagramController::unfavorite, roles(Roles.AUTHENTICATED))
              }
              get(diagramController::get, rolesOptionalAuthenticated)
              put(diagramController::update, roles(Roles.AUTHENTICATED))
              delete(diagramController::delete, roles(Roles.AUTHENTICATED))
          }
          get(diagramController::findById, rolesOptionalAuthenticated)
          post(diagramController::create, roles(Roles.AUTHENTICATED))
      }
          path("tags") {
              get(tagController::get, rolesOptionalAuthenticated)
          }
      }
  }
}
