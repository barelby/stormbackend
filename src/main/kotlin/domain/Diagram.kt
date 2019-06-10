package domain

import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.ResultRow
import java.util.UUID

data class DiagramDTO( val diagram: Diagram?)

data class DiagramsDTO(val diagrams: List<Diagram>, val diagramCount: Int)

data class Diagram (val uuid: String, val body: String) {


}
