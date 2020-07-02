import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.ktor.application.*
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

val module = module {
    factory<ApplicationCall> { ApplicationCallHolder.get() ?: throw IllegalStateException("There is no call at this moment.") }
}

object ApplicationCallHolder {
    val localValue = ThreadLocal<ApplicationCall>()

    fun with(value: ApplicationCall, block: suspend CoroutineScope.() -> Unit) {
        GlobalScope.launch(Dispatchers.Default + localValue.asContextElement(value), block=block)
    }

    fun get(): ApplicationCall? = localValue.get()
}

fun Application.main() {
    install(Koin) {
        SLF4JLogger()
        modules(module)
    }

    routing {
        intercept(ApplicationCallPipeline.Call) {
            ApplicationCallHolder.with(call) {
                proceed()
            }
        }
        get("/") {
            val c = application.get<ApplicationCall>()
            println(c)
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
