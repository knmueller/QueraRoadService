package com.kyle.quera.route

import com.kyle.quera.core.IntersectionsService
import com.kyle.quera.model.ErrorResponse
import com.kyle.quera.model.Intersection
import com.kyle.quera.model.PagedResponse
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
@Resource("intersections")
class IntersectionsRequest(
    @KtorFieldDescription("Intersections sort order")
    val sort: String = "createdAt,desc",
    @KtorFieldDescription("Intersections page number")
    val page: Int = 0,
    @KtorFieldDescription("Intersections size of page")
    val size: Int = 10
) {
    @Serializable
    @Resource("{id}")
    class Id(
        val parent: IntersectionsRequest = IntersectionsRequest(),
        @KtorFieldDescription("Intersection identifier")
        val id: Int
    )
}

// All Intersection routes
@Tag(["Intersections"])
fun Routing.configureIntersectionRoutes() {
    val svc: IntersectionsService by inject(IntersectionsService::class.java)

    @KtorResponds(
        mapping = [
            ResponseEntry("200", PagedResponse::class),
        ]
    )
    @KtorDescription(
        summary = "Get all intersections",
        description = "Returns intersections according to query params"
    )
    get<IntersectionsRequest> { intersection ->
        val pagedIntersections = svc.getResources(intersection)
        call.respond(pagedIntersections)
    }

    @KtorResponds(
        mapping = [
            ResponseEntry("200", Intersection::class),
            ResponseEntry("404", ErrorResponse::class, description = "Resource not found")
        ]
    )
    @KtorDescription(
        summary = "Get intersection by id",
        description = "Returns intersection by id"
    )
    get<IntersectionsRequest.Id> { intersection ->
        val foundIntersection = svc.getResource(intersection.id)
        call.respond(foundIntersection)
    }

    @KtorResponds(
        mapping = [
            ResponseEntry("200", Intersection::class),
            ResponseEntry("400", ErrorResponse::class, description = "Bad Request"),
            ResponseEntry("404", ErrorResponse::class, description = "Resource not found")
        ]
    )
    @KtorDescription(
        summary = "Update an intersection",
        description = "Returns the updated intersection resource"
    )
    put<IntersectionsRequest.Id> { intersection ->
        val intersectionBody = call.receive<Intersection>().apply { id = intersection.id }
        val updatedIntersection = svc.updateResource(intersectionBody)
        call.respond(HttpStatusCode.Companion.Created, updatedIntersection)
    }

    @KtorResponds(
        mapping = [
            ResponseEntry("201", Intersection::class),
            ResponseEntry("400", ErrorResponse::class, description = "Bad Request")
        ]
    )
    @KtorDescription(
        summary = "Create a new intersection",
        description = "Returns the new intersection resource"
    )
    post<IntersectionsRequest> {
        val intersectionBody = call.receive<Intersection>()
        val newIntersection = svc.createResource(intersectionBody)
        call.respond(HttpStatusCode.Companion.Created, newIntersection)
    }

    @KtorResponds(
        mapping = [
            ResponseEntry("204", Unit::class),
            ResponseEntry("404", ErrorResponse::class, description = "Resource not found")
        ]
    )
    @KtorDescription(
        summary = "Delete an intersection"
    )
    delete<IntersectionsRequest.Id> { intersection ->
        svc.deleteResource(intersection.id)
        call.respond(HttpStatusCode.NoContent)
    }
}
