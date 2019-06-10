package bpmn2

import config.AppConfig
import org.jetbrains.exposed.sql.Database

fun main(args: Array<String>) {
    println("Hello, World")
    AppConfig().setup().start()

}

