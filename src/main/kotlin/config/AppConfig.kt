package config

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import config.ModulesConfig.allModules
import io.javalin.Javalin
import io.javalin.JavalinEvent
import io.javalin.json.JavalinJackson
import io.javalin.security.AccessManager
import org.h2.tools.Server
import org.koin.core.KoinProperties
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext
import org.koin.standalone.getProperty
import org.koin.standalone.inject
import web.Router
import java.text.SimpleDateFormat

class AppConfig: KoinComponent {
    private val router: Router by inject()
    private val authConfig: AuthConfig by inject()

    fun setup(): Javalin {
        StandAloneContext.startKoin(allModules,
            KoinProperties(true,true))
        return Javalin.create()
            .also { app ->

                this.configureMapper()
                authConfig.configure(app)
                app.enableCorsForAllOrigins()
                    .contextPath(getProperty("context"))
                    .event(JavalinEvent.SERVER_STOPPING) {
                        StandAloneContext.stopKoin()
                        Server.createPgServer().stop()
                    }
                router.register(app)
                app.port(getProperty("server_port"))

            }
    }

    private fun configureMapper() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        JavalinJackson.configure(jacksonObjectMapper()
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setDateFormat(dateFormat)
            .configure(SerializationFeature.WRITE_DATES_WITH_ZONE_ID, true)
        )
    }
}
