package domain.repository

import domain.Diagram
import domain.User
import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.UUID
import javax.sql.DataSource

private object Diagrams : UUIDTable() {
    val title: Column<String> = varchar("title", 350)
    val description: Column<String> = varchar("description", 350)
    val body: Column<String> = text("body")
    val createdAt: Column<DateTime> = date("created_at")
    val updatedAt: Column<DateTime> = date("updated_at")
    val author: Column<Long> = long("author")
    fun toDomain(row: ResultRow, author: User?): Diagram {
        return Diagram(
            uuid = row[id].value,
            body = row[body],
            title = row[title],
            description = row[description],
            createdAt = row[createdAt].toDate(),
            updatedAt = row[updatedAt].toDate(),
            author = author

        )
    }
}

private object Favorites : Table() {
    val id: Column<UUID> = uuid("id").primaryKey()
    val user: Column<Long> = long("user").primaryKey()
}

private object DiagramTags : Table() {
    val tag: Column<Long> = long("tag").primaryKey()
    val id: Column<UUID> = Favorites.uuid("id").primaryKey()
}


class DiagramRepository(private val dataSource: DataSource) {
    init {
        transaction(Database.connect(dataSource)) {
            SchemaUtils.createMissingTablesAndColumns(Diagrams)
            SchemaUtils.createMissingTablesAndColumns(Favorites)
            SchemaUtils.createMissingTablesAndColumns(DiagramTags)
        }
    }

    fun create(diagram: Diagram): Diagram? {
        val id = transaction(Database.connect(dataSource)) {
            Diagrams.insertAndGetId { row ->
                row[body] = diagram.body
                row[title] = diagram.title!!
                row[description] = diagram.description!!
                row[createdAt] = DateTime()
                row[updatedAt] = DateTime()
                row[author] = diagram.author?.id!!
            }
        }.value

        diagram.tagList.map {tag ->
            Tags.slice(Tags.id).select {Tags.name eq tag}
                .map {row -> row[Tags.id].value}.firstOrNull() ?: Tags.insertAndGetId { it[name] = tag }.value
        }.also {
            DiagramTags.batchInsert(it) {
                tagId ->
                this[DiagramTags.tag] = tagId
                this[DiagramTags.id] = id
            }
        }
        return findById(id)
    }

    private fun findWithConditional(where: Op<Boolean>, limit: Int, offset: Int): List<Diagram> {
        return transaction(Database.connect(dataSource)) {
            Diagrams.join(Users, JoinType.INNER, additionalConstraint = { Diagrams.author eq Users.id })
                .select({ where })
                .limit(limit, offset)
                .sortedBy { Diagrams.createdAt }
                .map { row ->
                    val id = row[Diagrams.id]
                    val favoritesCount = Favorites.select { Favorites.id eq id.value }.count()
                    val tagList = Tags.join(DiagramTags, JoinType.INNER,
                        additionalConstraint = { Tags.id eq DiagramTags.tag })
                        .select { DiagramTags.id eq id.value }
                        .map { it[Tags.name] }
                    Diagrams.toDomain(row, Users.toDomain(row))
                        .copy(
                            favorited = favoritesCount > 0,
                            favoritesCount = favoritesCount.toLong(),
                            tagList = tagList
                        )


                }

        }
    }

    fun findById(id: UUID): Diagram? {
        return findWithConditional((Diagrams.id eq id), 1, 0).firstOrNull()

    }

}
