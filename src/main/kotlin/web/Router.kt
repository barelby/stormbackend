package web

import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.get
import io.javalin.apibuilder.ApiBuilder.path
import io.javalin.apibuilder.ApiBuilder.post
import org.koin.standalone.KoinComponent
import web.controllers.DiagramController

class Router(private val diagramController: DiagramController) : KoinComponent {
  fun register(app: Javalin) {
      app.routes {
      path("diagrams") {
          post(diagramController::create)
          get(diagramController::findById)
      }
      }
  }
}
