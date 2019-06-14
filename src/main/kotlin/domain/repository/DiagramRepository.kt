package domain.repository

import domain.Diagram
import domain.User
import io.javalin.NotFoundResponse
import org.jetbrains.exposed.dao.UUIDTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import java.util.UUID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
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
    val id: Column<UUID> = uuid("id").primaryKey()
}

class DiagramRepository(private val dataSource: DataSource) {
    init {
        transaction(Database.connect(dataSource)) {
            SchemaUtils.createMissingTablesAndColumns(Diagrams)
            SchemaUtils.createMissingTablesAndColumns(Favorites)
            SchemaUtils.createMissingTablesAndColumns(Tags)
            SchemaUtils.createMissingTablesAndColumns(DiagramTags)
        }
    }
    fun create(diagram: Diagram): Diagram? {
        val id = transaction(Database.connect(dataSource)) {
            addLogger(StdOutSqlLogger)
            Diagrams.insertAndGetId { row ->
                row[body] = diagram.body
                row[title] = diagram.title!!
                row[description] = diagram.description!!
                row[createdAt] = DateTime()
                row[updatedAt] = DateTime()
                row[author] = diagram.author?.id!!
            }
        }.value

        transaction(Database.connect(dataSource)) {
            addLogger(StdOutSqlLogger)
            diagram.tagList.map { tag ->
                Tags.slice(Tags.id).select { Tags.name eq tag }
                    .map { row -> row[Tags.id].value }.firstOrNull() ?: Tags.insertAndGetId { it[name] = tag }.value
            }.also {
                DiagramTags.batchInsert(it) { tagId ->
                    this[DiagramTags.tag] = tagId
                    this[DiagramTags.id] = id
                }
            }
        }

        return findById(id)
    }

    private fun findWithConditional(where: Op<Boolean>, limit: Int, offset: Int): List<Diagram> {
        return transaction(Database.connect(dataSource)) {
            Diagrams.join(Users, JoinType.INNER, additionalConstraint = { Diagrams.author eq Users.id })
                .select({ where })
                .limit(limit, offset)
                .orderBy( Diagrams.createdAt, true)
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

    fun findByTag(tag: String, limit: Int, offset: Int):  List<Diagram> {
        val  diagramsIds = transaction(Database.connect(dataSource)) {
            Tags.join(DiagramTags, joinType = JoinType.INNER, additionalConstraint = {Tags.id eq DiagramTags.tag })
                .select {Tags.name eq tag}
                .map{ it[DiagramTags.id]}
        }
        return findWithConditional((Diagrams.id inList diagramsIds), limit, offset)
    }

    fun findByFavorited (favorited: String, limit:Int, offset: Int): List<Diagram> {
        val diagramsIds = transaction(Database.connect(dataSource)) {
            Favorites.join(Users,JoinType.INNER, additionalConstraint = { Favorites.user eq Users.id})
                .slice(Favorites.id)
                .select{Users.username eq favorited}
                .map {it[Favorites.id]}
        }
        return findWithConditional((Diagrams.id inList diagramsIds), limit, offset)
        
    }

    fun findAll(limit: Int, offset: Int): List<Diagram> {
        return transaction(Database.connect(dataSource)) {
            Diagrams.join(Users, JoinType.INNER, additionalConstraint = { Diagrams.author eq Users.id })
                .selectAll()
                .limit(limit, offset)
                .orderBy(Diagrams.createdAt, true)
                .map { row ->
                    val favoritesCount = Favorites.select { Favorites.id eq row[Diagrams.id].value }.count()
                    Diagrams.toDomain(row, Users.toDomain(row))
                        .copy(favoritesCount = favoritesCount.toLong(), tagList =
                        Tags.join(Diagrams, JoinType.INNER,
                            additionalConstraint = { Tags.id eq DiagramTags.tag })
                            .select { DiagramTags.id eq row[Diagrams.id].value }
                            .map { it[Tags.name] })
                }
        }
    }

    fun findFeed(email: String, limit: Int, offset: Int): List<Diagram> {
        val authors = transaction(Database.connect(dataSource)) {
            Follows.join(Users, JoinType.INNER, additionalConstraint = { Follows.follower eq Users.id })
                .slice(Follows.user)
                .select { Users.email eq email }
                .map { it[Follows.user] }
        }
        return findWithConditional((Diagrams.author inList authors), limit, offset)
    }

    fun findByAuthor(author: String, limit: Int, offset: Int): List<Diagram> {
        return findWithConditional((Users.username eq author), limit, offset)
    }

    fun update(id: UUID, diagram: Diagram): Diagram? {
    return transaction(Database.connect(dataSource)){
        Diagrams.update({ Diagrams.id eq id}) { row ->
            if (diagram.title != null) {
                row[title] = diagram.title
            }
            if (diagram.description != null) {
                row[description] = diagram.description
            }
            row[body] = diagram.body
            row[updatedAt] = DateTime()
            if (diagram.author != null) {
                row[author] = diagram.author.id!!
            }
        }
        if (diagram.uuid != null) {
            Favorites.update({ Favorites.id eq id }) { row ->
                row[Favorites.id] = id
            }
        }
        Favorites.select {
            Favorites.id eq diagram.uuid!!
        }.count()

        }.let {
        findById(diagram.uuid!!)?.copy(favoritesCount = it.toLong())
    }
    }

    fun favorite(userId: Long, id: UUID): Int {
        return transaction(Database.connect(dataSource)) {
            Favorites.insert { row ->
                row[Favorites.id] = id
                row[Favorites.user] = userId
            }.let {
                Favorites.select { Favorites.id eq id }.count()
            }
        }
    }

    fun unfavorite(userId: Long, id: UUID): Int {
        val diagram = findById(id) ?: throw NotFoundResponse()
        return transaction(Database.connect(dataSource)) {
            Favorites.deleteWhere {
                Favorites.id eq diagram.uuid!! and (Favorites.user eq userId)
            }.let {
                Favorites.select { Favorites.id eq diagram.uuid!! }.count()
            }
        }
    }

    fun delete(id:UUID) {
        transaction(Database.connect(dataSource)) {
            Diagrams.deleteWhere { Diagrams.id eq id }
            Favorites.deleteWhere { Favorites.id eq id }
        }
    }

    }


