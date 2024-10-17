package com.common.ktor.server

import com.kyle.quera.Utils
import com.kyle.quera.model.BadRequestErrorRsp
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

fun Application.configureCommonValidation() {
    // Validations and Status Pages
    // https://medium.com/hyperskill/creating-your-reactive-rest-api-with-kotlin-and-ktor-part-ii-7b6b087f61e7

    // Content Validation
    // implementation("io.ktor:ktor-server-request-validation:$ktor_version")

//    fun Application.configureValidation() {
//        install(RequestValidation) {
//            racketValidation()
//        }
//    }

//    fun RequestValidationConfig.racketValidation() {
//        validate<RacketRequest> { racket ->
//            if (racket.brand.isBlank() || racket.brand.length < 3) {
//                ValidationResult.Invalid("Brand must be at least 3 characters long")
//            } else if (racket.model.isBlank()) {
//                ValidationResult.Invalid("Model must not be empty")
//            } else if (racket.price < 0.0) {
//                ValidationResult.Invalid("Price must be positive value or zero")
//            } else if (racket.numberTenisPlayers < 0) {
//                ValidationResult.Invalid("Number of tennis players must be positive number or zero")
//            } else {
//                // Everything is ok!
//                ValidationResult.Valid
//            }
//        }
//    }
}

fun Application.configureCommonStatusPages() {
    // Validations and Status Pages
    // https://medium.com/hyperskill/creating-your-reactive-rest-api-with-kotlin-and-ktor-part-ii-7b6b087f61e7

    // Server Status Pages
    // implementation("io.ktor:ktor-server-status-pages:$ktor_version")

    install(StatusPages) {
        exception<NoSuchElementException> { call, cause ->
            call.respond(HttpStatusCode.NotFound, NotFoundErrorRsp)
        }
        exception<BadRequestException> { call, cause ->
            val errDetails = if (cause.message != null) cause.message!! else ""
            call.respond(HttpStatusCode.BadRequest, BadRequestErrorRsp.apply { details = errDetails })
        }
    }
}