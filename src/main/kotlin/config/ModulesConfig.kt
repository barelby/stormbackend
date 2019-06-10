package config
import domain.repository.DiagramRepository
import domain.service.DiagramService
import org.koin.dsl.module.module
import web.Router
import web.controllers.DiagramController

object ModulesConfig {
    private val configModule = module {
        single {AppConfig()}
        single {
            DbConfig(getProperty("jdbc.url"), getProperty("db.username"), getProperty("db.password")).getDataSource()
        }
        single { Router(get()) }
    }
    private val diagramModule = module {
        single { DiagramController(get())}
        single { DiagramService(get())}
        single { DiagramRepository(get())}
    }

    internal  val allModules = listOf(ModulesConfig.diagramModule , ModulesConfig.configModule)

}
