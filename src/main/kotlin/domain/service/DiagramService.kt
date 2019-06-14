package domain.service

import domain.Diagram
import domain.repository.DiagramRepository
import domain.repository.UserRepository
import io.javalin.BadRequestResponse
import io.javalin.InternalServerErrorResponse
import io.javalin.NotFoundResponse
import java.util.UUID

class DiagramService(private val diagramRepository: DiagramRepository,
                     private val userRepository: UserRepository) {
    fun findBy(tag: String?, author: String?, favorited: String?, limit: Int, offset: Int): List<Diagram> {
        return when {
            !tag.isNullOrBlank() -> diagramRepository.findByTag(tag,limit,offset)
            !author.isNullOrBlank() -> diagramRepository.findByAuthor(author,limit,offset)
            !favorited.isNullOrBlank() -> diagramRepository.findByFavorited(favorited, limit,offset)
            else -> diagramRepository.findAll(limit, offset)
        }
    }

    fun create(email: String?, diagram: Diagram): Diagram? {
      email ?: throw BadRequestResponse("invalid user to create diagram")
      return userRepository.findByEmail(email).let { author ->
          author ?: throw BadRequestResponse("invalid user to create diagram")
          diagramRepository.create(diagram.copy( author = author)) ?: throw InternalServerErrorResponse("Error create diagram")

      }



  }

    fun findById(id: UUID): Diagram? {
        return diagramRepository.findById(id)   }

    fun update(id: UUID, diagram: Diagram): Diagram? {
        return findById(id).run {
            diagramRepository.update(id, diagram.copy(uuid = id))
        }
    }

    fun findFeed(email: String?, limit: Int, offset: Int): List<Diagram> {
        email ?: throw BadRequestResponse("invalid user to find feeds")
        return diagramRepository.findFeed(email, limit, offset)
    }

    fun favorite(email: String?, id: UUID): Diagram {
        email ?: throw BadRequestResponse("invalid user to favorite article")
        val diagram = findById(id) ?: throw NotFoundResponse()
        return userRepository.findByEmail(email).let { user ->
            user ?: throw BadRequestResponse()
            diagramRepository.favorite(user.id!!, id)
                .let { favoritesCount ->
                    diagram.copy(favorited = true, favoritesCount = favoritesCount.toLong())
                }
        }
    }

    fun unfavorite(email: String?, id: UUID): Diagram {
        email ?: throw BadRequestResponse("invalid user to favorite article")
        val diagram = findById(id) ?: throw NotFoundResponse()
        return userRepository.findByEmail(email).let { user ->
            user ?: throw BadRequestResponse()
            diagramRepository.unfavorite(user.id!!, id)
                .let { favoritesCount ->
                    diagram.copy(favorited = true, favoritesCount = favoritesCount.toLong())
                }
        }
    }

    fun delete(id: UUID) {
        return diagramRepository.delete(id)
    }
}
