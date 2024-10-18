package com.kyle.quera.route

import com.kyle.quera.core.RoadsService
import com.kyle.quera.model.ErrorResponse
import com.kyle.quera.model.PagedResponse
import com.kyle.quera.model.Road
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
@Resource("roads")
class RoadsRequest(
    @KtorFieldDescription("Intersection identifier for GET association")
    val intersectionId: Int? = null,
    @KtorFieldDescription("Roads sort order")
    val sort: String = "createdAt,desc",
    @KtorFieldDescription("Roads page number")
    val page: Int = 0,
    @KtorFieldDescription("Roads size of page")
    val size: Int = 10
) {
    @Serializable
    @Resource("{id}")
    class Id(
        val parent: RoadsRequest = RoadsRequest(),
        @KtorFieldDescription("Road identifier")
        val id: Int
    )
}

// All Road routes
@Tag(["Roads"])
fun Routing.configureRoadRoutes() {
    val svc: RoadsService by inject(RoadsService::class.java)

    @KtorResponds(
        mapping = [
            ResponseEntry("200", PagedResponse::class),
        ]
    )
    @KtorDescription(
        summary = "Get all roads",
        description = "Returns roads according to query params"
    )
    get<RoadsRequest> { road ->
        println("INJECTED get ${svc::class.java}")

        val pagedRoads = svc.getResources(road)
        call.respond(pagedRoads)
    }

    @KtorResponds(
        mapping = [
            ResponseEntry("200", Road::class),
            ResponseEntry("404", ErrorResponse::class, description = "Resource not found")
        ]
    )
    @KtorDescription(
        summary = "Get road by id",
        description = "Returns road by id"
    )
    get<RoadsRequest.Id> { road ->
        println("INJECTED get ${svc::class.java}")

        val foundRoad = svc.getResource(road.id)
        call.respond(foundRoad)
    }

    @KtorResponds(
        mapping = [
            ResponseEntry("200", Road::class),
            ResponseEntry("400", ErrorResponse::class, description = "Bad Request"),
            ResponseEntry("404", ErrorResponse::class, description = "Resource not found")
        ]
    )
    @KtorDescription(
        summary = "Update a road",
        description = "Returns the updated road resource"
    )
    put<RoadsRequest.Id> { road ->
        val roadBody = call.receive<Road>().apply { id = road.id }
        val updatedRoad = svc.updateResource(roadBody)
        call.respond(HttpStatusCode.Companion.Created, updatedRoad)
    }

    @KtorResponds(
        mapping = [
            ResponseEntry("201", Road::class),
            ResponseEntry("400", ErrorResponse::class, description = "Bad Request")
        ]
    )
    @KtorDescription(
        summary = "Create a new road",
        description = "Returns the new road resource"
    )
    post<RoadsRequest> {
        val roadBody = call.receive<Road>()
        val newRoad = svc.createResource(roadBody)
        call.respond(HttpStatusCode.Companion.Created, newRoad)
    }

    @KtorResponds(
        mapping = [
            ResponseEntry("204", Unit::class),
            ResponseEntry("404", ErrorResponse::class, description = "Resource not found")
        ]
    )
    @KtorDescription(
        summary = "Delete a road"
    )
    delete<RoadsRequest.Id> { road ->
        svc.deleteResource(road.id)
        call.respond(HttpStatusCode.NoContent)
    }
}
