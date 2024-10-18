package com.common.ktor.server

import com.kyle.quera.Utils
import com.kyle.quera.model.BadRequestErrorRsp
import com.kyle.quera.model.InternalServerErrorRsp
import com.kyle.quera.model.NotFoundErrorRsp
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import java.util.NoSuchElementException

fun Application.configureCommonCors() {
    install(CORS) {
        anyHost()
    }
}

fun Application.configureCommonContentNegotiation() {
    install(ContentNegotiation) {
        json(Utils.json)
    }
}

fun Application.configureCommonStatusPages() {
    install(StatusPages) {
        exception<NoSuchElementException> { call, _ ->
            call.respond(HttpStatusCode.NotFound, NotFoundErrorRsp)
        }
        exception<BadRequestException> { call, cause ->
            val errDetails = if (cause.message != null) cause.message!! else ""
            call.respond(HttpStatusCode.BadRequest, BadRequestErrorRsp.apply { details = errDetails })
        }
        exception<Exception> { call, _ ->
            call.respond(HttpStatusCode.InternalServerError, InternalServerErrorRsp)
        }
    }
}