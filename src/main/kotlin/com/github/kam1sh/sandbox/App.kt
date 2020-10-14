package com.github.kam1sh.sandbox

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.github.kam1sh.krait.core.Krait
import com.github.kam1sh.krait.core.props.FilePropertiesSource
import com.github.kam1sh.krait.core.props.SystemPropertiesSource
import com.github.kam1sh.krait.yaml.YamlSource
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.*
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.koin.logger.SLF4JLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val module = module {
    factory<Krait> { KraitHolder.localValue ?: throw IllegalStateException("There is no Krait at this moment.") }
}

object KraitHolder {
    var localValue: Krait? = null
}

fun Application.main() {
    install(Koin) {
        SLF4JLogger()
        modules(module)
    }

    routing {
        post("/reload") {
            application.get<Krait>().load("prod")
        }
        route("/secure") {
            intercept(ApplicationCallPipeline.Call) {
                val token = call.request.header("Token")
                if (token == null) {
                    call.respond("Token required")
                    finish()
                } else {
                    val allowTokens = application.get<Krait>()["tokens"].list().map { it.text() }.toSet()
                    if (allowTokens.contains(token)) {
                        proceed()
                    } else {
                        call.respond(HttpStatusCode.Forbidden)
                        finish()
                    }
                }
            }
            get("/") {
                call.respond("Hello!")
            }
        }
    }
}



fun main(args: Array<String>) {
    val lc = LoggerFactory.getILoggerFactory() as LoggerContext
    val kr = Krait {
        sources {
            add(SystemPropertiesSource("app"))
            add(FilePropertiesSource("app"))
            add(YamlSource("app"))
            add(FilePropertiesSource("configs/app.properties", classLoader = javaClass.classLoader))
        }
    }
    kr.load("prod")
    KraitHolder.localValue = kr
    for (item in kr["loggers"].list()) {
        val name = item["name"].text()
        val level = Level.valueOf(item["level"].text())
        lc.getLogger(name).level = level
    }
    embeddedServer(
        Netty, port = kr["server"]["port"].int(), module = Application::main
    ).start(wait = true)
}
