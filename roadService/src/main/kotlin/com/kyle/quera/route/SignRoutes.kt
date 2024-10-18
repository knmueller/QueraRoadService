package com.kyle.quera.route

import com.kyle.quera.core.SignsService
import com.kyle.quera.model.ErrorResponse
import com.kyle.quera.model.PagedResponse
import com.kyle.quera.model.Sign
import io.github.tabilzad.ktor.annotations.KtorDescription
import io.github.tabilzad.ktor.annotations.KtorFieldDescription
import io.github.tabilzad.ktor.annotations.KtorResponds
import io.github.tabilzad.ktor.annotations.ResponseEntry
import io.github.tabilzad.ktor.annotations.Tag
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.response.respond
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.routing.Routing
import kotlinx.serialization.Serializable
import org.koin.java.KoinJavaComponent.inject

@Serializable
@Resource("signs")
class SignsRequest(
    @KtorFieldDescription("Road identifier for GET association")
    val roadId: Int? = null,
    @KtorFieldDescription("Signs sort order")
    val sort: String = "createdAt,desc",
    @KtorFieldDescription("Signs page number")
    val page: Int = 0,
    @KtorFieldDescription("Signs size of page")
    val size: Int = 10
) {
    @Serializable
    @Resource("{id}")
    class Id(
        val parent: SignsRequest = SignsRequest(),
        @KtorFieldDescription("Sign identifier")
        val id: Int
    )
}

// All Sign routes
@Tag(["Signs"])
fun Routing.configureSignRoutes() {
    val svc: SignsService by inject(SignsService::class.java)

    // Get all signs
    @KtorResponds(
        mapping = [
            ResponseEntry("200", PagedResponse::class),
        ]
    )
    @KtorDescription(
        summary = "Get all signs",
        description = "Returns signs according to query params"
    )
    get<SignsRequest> { sign ->
        println("INJECTED get ${svc::class.java}")

        val pagedSigns = svc.getResources(sign)
        call.respond(pagedSigns)
    }

    // Get a single sign
    @KtorResponds(
        mapping = [
            ResponseEntry("200", Sign::class),
            ResponseEntry("404", ErrorResponse::class, description = "Resource not found")
        ]
    )
    @KtorDescription(
        summary = "Get sign by id",
        description = "Returns sign by id"
    )
    get<SignsRequest.Id> { sign ->
        println("INJECTED get ${svc::class.java}")

        val foundSign = svc.getResource(sign.id)
        call.respond(foundSign)
    }

    @KtorResponds(
        mapping = [
            ResponseEntry("200", Sign::class),
            ResponseEntry("400", ErrorResponse::class, description = "Bad Request"),
            ResponseEntry("404", ErrorResponse::class, description = "Resource not found")
        ]
    )
    @KtorDescription(
        summary = "Update a sign",
        description = "Returns the updated sign resource"
    )
    put<SignsRequest.Id> { sign ->
        val signBody = call.receive<Sign>().apply { id = sign.id }
        val updatedSign = svc.updateResource(signBody)
        call.respond(HttpStatusCode.Companion.Created, updatedSign)
    }

    // Create a new Sign
    @KtorResponds(
        mapping = [
            ResponseEntry("201", Sign::class),
            ResponseEntry("400", ErrorResponse::class, description = "Bad Request")
        ]
    )
    @KtorDescription(
        summary = "Create a new sign",
        description = "Returns the new sign resource"
    )
    post<SignsRequest> {
        val signBody = call.receive<Sign>()
        val newSign = svc.createResource(signBody)
        call.respond(HttpStatusCode.Companion.Created, newSign)
    }

    // Delete a sign
    @KtorResponds(
        mapping = [
            ResponseEntry("204", Unit::class),
            ResponseEntry("404", ErrorResponse::class, description = "Resource not found")
        ]
    )
    @KtorDescription(
        summary = "Delete a sign"
    )
    delete<SignsRequest.Id> { sign ->
        svc.deleteResource(sign.id)
        call.respond(HttpStatusCode.NoContent)
    }
}
