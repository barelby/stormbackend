package web.controllers

import domain.Diagram
import domain.DiagramDTO
import domain.service.DiagramService
import io.javalin.Context
import java.util.UUID

class DiagramController(private val diagramService: DiagramService) {

    fun create(ctx: Context) {
       ctx.bodyValidator<DiagramDTO>()
           .check({!it.diagram?.body.isNullOrBlank()})
           .get().diagram?.also {
           diagramService.create(email = null, diagram = it).apply { ctx.json(DiagramDTO(this)) }
       }
    }
    fun findById(ctx: Context) {
       val uuid = UUID.fromString(ctx.queryParam("uuid"))
        val limit = ctx.queryParam("limit") ?: "20"
        val offset = ctx.queryParam("offset") ?: "0"
        if (uuid != null) {
            diagramService.findById(uuid).also { diagram -> ctx.json(DiagramDTO(diagram))  }
        }
    }
}
