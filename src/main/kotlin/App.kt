import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun Application.main() {

    install(Authentication) {
        basic {
            realm = "hi there"
            validate { creds ->
                if (creds.name == "test" && creds.password == "123") UserIdPrincipal(creds.name) else null
            }
        }
    }
    val writeAuthPhase = PipelinePhase("WriteAuth")
    routing {
        route("/test") {
            val testRoute = authenticate {
                get {
                    call.respondText("123")
                }
            }
            val item = testRoute.items.find { it.name == "Challenge" }
            println(item)
            testRoute.insertPhaseBefore(Authentication.ChallengePhase, writeAuthPhase)
            testRoute.intercept(writeAuthPhase) {
                println(call.response.headers.allValues().toMap())
                proceed()
            }
            println(testRoute.items)

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
