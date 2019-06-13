package config
import domain.repository.DiagramRepository
import domain.repository.TagRepository
import domain.repository.UserRepository
import domain.service.DiagramService
import domain.service.TagService
import domain.service.UserService
import org.koin.dsl.module.module
import utils.JwtProvider
import web.Router
import web.controllers.DiagramController
import web.controllers.TagController
import web.controllers.UserController

object ModulesConfig {
    private val configModule = module {
        single { AppConfig() }
        single { JwtProvider() }
        single { AuthConfig(get()) }
        single {
            DbConfig(getProperty("jdbc.url"), getProperty("db.username"), getProperty("db.password")).getDataSource()
        }
        single { Router(get(), get() )}
    }
    private val userModule = module {
        single { UserController(get()) }
        single { UserService(get(), get()) }
        single { UserRepository(get()) }
    }
    private val diagramModule = module {
        single { DiagramController(get()) }
        single { DiagramService(get(), get()) }
        single { DiagramRepository(get()) }
    }

    private val tagModule = module {
        single { TagController(get()) }
        single { TagService(get()) }
        single { TagRepository(get()) }
    }
    internal val allModules = listOf(ModulesConfig.configModule, ModulesConfig.userModule,
        ModulesConfig.diagramModule,
        ModulesConfig.tagModule)
}