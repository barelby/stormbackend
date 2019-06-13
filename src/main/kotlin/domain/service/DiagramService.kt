package domain.service

import domain.Diagram
import domain.repository.DiagramRepository
import domain.repository.UserRepository
import io.javalin.BadRequestResponse
import io.javalin.InternalServerErrorResponse
import java.util.UUID

class DiagramService(private val diagramRepository: DiagramRepository,
                     private val userRepository: UserRepository) {

  fun create(email: String?, diagram: Diagram): Diagram? {
      email ?: throw BadRequestResponse("invalid user to create diagram")


      return userRepository.findByEmail(email).let { author ->
          author ?: throw BadRequestResponse("invalid user to create diagram")
          diagramRepository.create(diagram.copy( author = author)) ?: throw InternalServerErrorResponse("Error create diagram")

      }



  }

    fun findById(uuid: UUID): Diagram? {
        return diagramRepository.findById(uuid)   }

}
