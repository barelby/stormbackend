package domain.repository

import domain.Diagram
import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.deleteIgnoreWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import javax.sql.DataSource

private object Diagrams: UUIDTable() {
    val uuid = varchar("uuid",length = 512).primaryKey()
    var body = text("body")

    fun toDomain(row: ResultRow): Diagram {
        return Diagram(
            uuid = row[uuid],
            body = row[body]
        )
    }
}

class DiagramRepository(private val dataSource: DataSource) {
    init {
        transaction(Database.connect(dataSource)) {
            SchemaUtils.create(Diagrams)
        }
    }

    fun create(diagram: Diagram): Diagram? {
        transaction(Database.connect(dataSource)) {
            Diagrams.insert { row ->
                row[uuid] = diagram.uuid
                row[body] = diagram.body
            }
        }
       return findById(diagram.uuid)
    }

    private fun findWithConditional(where: Op<Boolean>, limit: Int, offset: Int): List<Diagram> {
       return  transaction(Database.connect(dataSource)) {
            Diagrams
                .select( {where})
                .limit(limit, offset)
                .map { row ->
                    var uuid = row[Diagrams.uuid]
                    var body = row[Diagrams.body]
                 Diagrams.toDomain(row)
                }

        }
    }
    fun findById(uuid: String): Diagram? {
        return findWithConditional((Diagrams.uuid eq uuid),1,0).firstOrNull()

    }

}
