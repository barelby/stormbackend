package domain.service

import domain.Diagram
import domain.repository.DiagramRepository
import io.javalin.BadRequestResponse
import io.javalin.InternalServerErrorResponse
import java.util.UUID

class DiagramService(private val diagramRepository: DiagramRepository) {

  fun create( uuid: String?, diagram: Diagram): Diagram? {
      uuid ?: throw BadRequestResponse("invalid uuid")
      return diagramRepository.create(diagram.copy(uuid = uuid, body =diagram.body)) ?: throw InternalServerErrorResponse("Error create diagram")
  }

    fun findById(uuid: String): Diagram? {
        return diagramRepository.findById(uuid)   }

}
