package com.kyle.quera

import com.common.ktor.server.configureCommonContentNegotiation
import com.common.ktor.server.configureCommonCors
import com.common.ktor.server.configureCommonMetrics
import com.common.ktor.server.configureCommonStatusPages
import com.kyle.quera.config.configureKoin
import com.kyle.quera.route.configureIntersectionRoutes
import com.kyle.quera.route.configureRoadRoutes
import com.kyle.quera.route.configureSignRoutes
import io.github.tabilzad.ktor.annotations.GenerateOpenApi
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.swagger.swaggerUI
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
//@GenerateOpenApi  // Disabled due to issue with docker build
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
    configureCommonMetrics()

    install(Resources)
    rootPath = "/api/1"
    routing {
        configureIntersectionRoutes()
        configureRoadRoutes()
        configureSignRoutes()
        swaggerUI("swagger","openapi/openapi.yaml"){
            version = "5.17.12"
        }
    }

    // jdbc:h2:mem:road_db
    try {
        // Start an H2 webserver to view the database
        Server.createWebServer("-web", "-webAllowOthers", "-webPort", "8082").start()
    } catch (e: JdbcSQLNonTransientConnectionException) {
        if (e.cause !is BindException) {
            throw e
        }
        // ignore. already running.
    }
}
