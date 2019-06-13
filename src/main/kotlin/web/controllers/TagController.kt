package web.controllers

import domain.service.TagService
import io.javalin.Context

class TagController(private val tagService: TagService) {
    fun get(ctx: Context) {
        tagService.findAll().also { tagDto ->
            ctx.json(tagDto)
        }
    }
}