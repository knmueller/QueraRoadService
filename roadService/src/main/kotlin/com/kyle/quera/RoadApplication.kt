package com.kyle.quera

import com.common.ktor.server.configureCommonContentNegotiation
import com.common.ktor.server.configureCommonCors
import com.common.ktor.server.configureCommonStatusPages
import com.kyle.quera.config.configureKoin
import com.kyle.quera.route.configureIntersectionRoutes
import com.kyle.quera.route.configureRoadRoutes
import com.kyle.quera.route.configureSignRoutes
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.resources.Resources
import io.ktor.server.routing.routing
import kotlinx.serialization.ExperimentalSerializationApi
import org.h2.jdbc.JdbcSQLNonTransientConnectionException
import org.h2.tools.Server
import org.koin.core.context.GlobalContext
import org.koin.ktor.plugin.KoinApplicationStarted
import java.net.BindException

fun main(args: Array<String>): Unit = EngineMain.main(args)

@ExperimentalSerializationApi
fun Application.module() {
    monitor.subscribe(KoinApplicationStarted) {
        log.info("Koin started.")
    }

    // this seems to be needed for unit testing so Koin is not set up more than once.
    if (GlobalContext.getOrNull() == null) {
        configureKoin()
    }
    configureCommonCors()
    configureCommonContentNegotiation()
    configureCommonStatusPages()

    install(Resources)
//    install(NotarizedApplication()) {
//        spec = {
//            OpenApiSpec(
//                "3.1.0",
//                "https://json-schema.org/draft/2020-12/schema",
//                info = Info(
//                    title = "Golf League API",
//                    version = "1.0",
//                    description = "Golf League Administration",
//                    contact = Contact(
//                        name = "Kyle Mueller",
//                        email = "kylenmueller@gmail.com",
//                    )
//                ),
//                servers = mutableListOf(
//                    io.bkbn.kompendium.oas.server.Server(
//                        url = URI("http://localhost:8081"),
//                        description = "local instance of my API"
//                    )
//                ),
//                tags = mutableListOf(
//                    Tag("courses", "courses api")
//                )
//            )
//        }
//    }
    rootPath = "/api/1"
    routing {
//        swaggerUI("/swagger", "/openapi.json")
//        swagger()
//        redoc()
        configureIntersectionRoutes()
        configureRoadRoutes()
        configureSignRoutes()
    }

    // jdbc:h2:mem:road_db
    try {
        Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start()
    } catch (e: JdbcSQLNonTransientConnectionException) {
        if (e.cause !is BindException) {
            throw e
        }
        // ignore. already running.
    }
}