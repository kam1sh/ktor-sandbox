import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import kotlinx.coroutines.*
import org.koin.dsl.module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.get
import org.koin.ktor.ext.modules
import org.koin.logger.SLF4JLogger
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File


fun Application.main() {
    routing {
        get("/") {
            File("image.png").inputStream().use {
                call.respondOutputStream(ContentType.parse("image/png")) {
                    it.copyTo(this)
                }
            }
        }
    }
}



fun main(args: Array<String>) {
    val lc = LoggerFactory.getILoggerFactory() as LoggerContext
    lc.getLogger(Logger.ROOT_LOGGER_NAME).level = Level.WARN
    embeddedServer(
        Netty, port = 8080, module = Application::main
    ).start(wait = true)
}
