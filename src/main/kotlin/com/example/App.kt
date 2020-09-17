package com.example

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.jackson.*
import io.ktor.response.respond
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*


val mod = module {
    single<Database> { DatabaseImpl() }
}

fun Application.main() {

    install(Koin) {
        modules(mod)
    }

    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        }
    }

    routing {
        route("/items") {
            get("/") {
                val db = application.get<Database>()
                call.respond(db.getItems())
            }
        }
    }
}



fun main() {
    val props = Properties()
    File("config.properties").inputStream().use {
        props.load(it)
    }
    props.loadConfig()


    val lc = LoggerFactory.getILoggerFactory() as LoggerContext
    lc.getLogger(Logger.ROOT_LOGGER_NAME).level = Level.WARN
    embeddedServer(
        Netty,
//        watchPaths = listOf("."),
        port = 8080,
        module = Application::main
    ).apply { start(wait = true) }
}
