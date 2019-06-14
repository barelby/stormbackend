package web.controllers

import bpmn2.util.HttpUtil
import config.AppConfig
import domain.Diagram
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import domain.DiagramDTO
import domain.TagDTO
import io.javalin.Javalin
import org.eclipse.jetty.http.HttpStatus
import org.junit.Before
import org.junit.Test

class TagControllerTest {
    private lateinit var app: Javalin
    private lateinit var http: HttpUtil

    @Before
    fun start() {
        app = AppConfig().setup().start()
        http = HttpUtil(app.port())
    }

    @Test
    fun `get all tags`() {
        val diagram = Diagram(title = "How to train your dragon",
            description = "Ever wonder how?",
            body = "Very carefully.",
            uuid = null,
            tagList = listOf("dragons", "training"))
        val email = "create_article1@valid_emai1l.com"
        val password = "Test"
        http.registerUser(email, password, "user_name_test11")
        http.loginAndSetTokenHeader(email, password)

        http.post<DiagramDTO>("/api/articles", DiagramDTO(diagram))

        val response = http.get<TagDTO>("/api/tags")

        assertEquals(response.status, HttpStatus.OK_200)
        assertTrue(response.body.tags.isNotEmpty())
    }

}
