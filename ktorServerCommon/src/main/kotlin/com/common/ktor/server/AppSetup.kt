package com.common.ktor.server

import com.codahale.metrics.Slf4jReporter
import com.kyle.quera.Utils
import com.kyle.quera.model.BadRequestErrorRsp
import com.kyle.quera.model.InternalServerErrorRsp
import com.kyle.quera.model.NotFoundErrorRsp
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.metrics.dropwizard.DropwizardMetrics
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import java.util.NoSuchElementException
import java.util.concurrent.TimeUnit

// Add a default CORS config just accepting any host.
fun Application.configureCommonCors() {
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }
}

// Add json for content negotiation
fun Application.configureCommonContentNegotiation() {
    install(ContentNegotiation) {
        json(Utils.json)
    }
}

// Add common global exception handling for uncaught or known errors.
fun Application.configureCommonStatusPages() {
    install(StatusPages) {
        exception<NoSuchElementException> { call, _ ->
            call.respond(HttpStatusCode.NotFound, NotFoundErrorRsp)
        }
        exception<BadRequestException> { call, cause ->
            val errDetails = if (cause.message != null) cause.message!! else ""
            call.respond(HttpStatusCode.BadRequest, BadRequestErrorRsp.apply { details = errDetails })
        }
        exception<Exception> { call, cause ->
            cause.printStackTrace()
            call.respond(HttpStatusCode.InternalServerError, InternalServerErrorRsp)
        }
    }
}

fun Application.configureCommonMetrics() {
    val logger = this.log
    install(DropwizardMetrics) {
        Slf4jReporter.forRegistry(registry)
            .outputTo(logger)
            .convertRatesTo(TimeUnit.SECONDS)
            .convertDurationsTo(TimeUnit.MILLISECONDS)
            .build()
            .start(10, TimeUnit.SECONDS)
    }
}