package web.controllers

import domain.Diagram
import domain.DiagramDTO
import domain.DiagramsDTO
import domain.service.DiagramService
import io.javalin.Context
import java.util.UUID

class DiagramController(private val diagramService: DiagramService) {
    fun findBy(ctx: Context) {
        val tag = ctx.queryParam("tag")
        val author = ctx.queryParam("author")
        val favorited = ctx.queryParam("favorited")
        val limit = ctx.queryParam("limit") ?: "20"
        val offset = ctx.queryParam("offset") ?: "0"
        diagramService.findBy(tag, author, favorited, limit.toInt(), offset.toInt()).also { articles ->
            ctx.json(DiagramsDTO(articles, articles.size))
        }
    }
    fun feed(ctx: Context) {
        val limit = ctx.queryParam("limit") ?: "20"
        val offset = ctx.queryParam("offset") ?: "0"
        diagramService.findFeed(ctx.attribute("email"), limit.toInt(), offset.toInt()).also { articles ->
            ctx.json(DiagramsDTO(articles, articles.size))
        }
    }
    fun get(ctx: Context) {
        ctx.validatedPathParam("id")
            .check({ it.isNotBlank() })
            .getOrThrow().also { id ->
                diagramService.findById(UUID.fromString(id)).apply {
                    ctx.json(DiagramDTO(this))
                }
            }
    }
    fun create(ctx: Context) {
        ctx.validatedBody<DiagramDTO>()
            .check({ !it.diagram?.title.isNullOrBlank() })
            .check({ !it.diagram?.description.isNullOrBlank() })
            .check({ !it.diagram?.body.isNullOrBlank() })
            .getOrThrow().diagram?.also { diagram ->
            diagramService.create(ctx.attribute("email"), diagram).apply {
                ctx.json(DiagramDTO(this))
            }
        }
    }
    fun update(ctx: Context) {
        val id = ctx.validatedPathParam("id").getOrThrow()
        ctx.validatedBody<DiagramDTO>()
            .check({ !it.diagram?.body.isNullOrBlank() })
            .getOrThrow().diagram?.also { diagram ->
            diagramService.update(UUID.fromString(id),diagram).apply {
                ctx.json(DiagramDTO(this))
            }
        }
    }
    fun delete(ctx: Context) {
        ctx.validatedPathParam("id").getOrThrow().also { id ->
            diagramService.delete(id = UUID.fromString(id))
        }
    }
    fun favorite(ctx: Context) {
        ctx.validatedPathParam("id").getOrThrow().also { id ->
            diagramService.favorite(ctx.attribute("email"), UUID.fromString(id)).apply {
                ctx.json(DiagramDTO(this))
            }
        }
    }
    fun unfavorite(ctx: Context) {
        ctx.validatedPathParam("id").getOrThrow().also { id ->
            diagramService.unfavorite(ctx.attribute("email"), UUID.fromString(id)).apply {
                ctx.json(DiagramDTO(this))
            }
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
