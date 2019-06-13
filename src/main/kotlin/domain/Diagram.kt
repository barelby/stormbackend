package domain

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ResultRow
import java.util.*

data class DiagramDTO( val diagram: Diagram?)

data class DiagramsDTO(val diagrams: List<Diagram>, val diagramCount: Int)

data class Diagram (val uuid: UUID?,
                    val title: String?,
                    val description: String?,
                    val body: String,
                    val tagList: List<String> = listOf(),
                    val createdAt: Date? = null,
                    val updatedAt: Date? = null,
                    val favorited: Boolean = false,
                    val private: Boolean = true,
                    val favoritesCount: Long = 0,
                    val author: User? = null) {


}
