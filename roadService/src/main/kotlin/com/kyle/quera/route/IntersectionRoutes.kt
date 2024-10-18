package com.kyle.quera.route

import com.kyle.quera.core.IntersectionsService
import com.kyle.quera.model.Intersection
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.response.respond
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import kotlinx.serialization.Serializable
import org.koin.java.KoinJavaComponent.inject

@Serializable
@Resource("intersections")
class IntersectionsRequest(val sort: String = "createdAt,desc", val page: Int = 0, val size: Int = 10) {
    @Serializable
    @Resource("{id}")
    class Id(val parent: IntersectionsRequest = IntersectionsRequest(), val id: Int)
}

// All Intersection routes
fun Routing.configureIntersectionRoutes() {
    val svc: IntersectionsService by inject(IntersectionsService::class.java)

    intersectionDocumentation()
    get<IntersectionsRequest> { intersection ->
        println("INJECTED get ${svc::class.java}")

        val pagedIntersections = svc.getResources(intersection)
        call.respond(pagedIntersections)
    }

    get<IntersectionsRequest.Id> { intersection ->
        println("INJECTED get ${svc::class.java}")

        val foundIntersection = svc.getResource(intersection.id)
        call.respond(foundIntersection)
    }

    post<IntersectionsRequest> {
        val intersectionBody = call.receive<Intersection>()
        val newIntersection = svc.createResource(intersectionBody)
        call.respond(HttpStatusCode.Companion.Created, newIntersection)
    }

    delete<IntersectionsRequest.Id> { intersection ->
        svc.deleteResource(intersection.id)
        call.respond(HttpStatusCode.NoContent)
    }
}

fun Route.intersectionDocumentation() {
    // TODO

//    install(NotarizedResource<CoursesRequest>()) {
//        tags = setOf("course-api")
//        parameters = listOf(
//            Parameter(
//                name = "sort",
//                `in` = Parameter.Location.query,
//                schema = TypeDefinition.STRING
//            ),
//            Parameter(
//                name = "page",
//                `in` = Parameter.Location.query,
//                schema = TypeDefinition.INT
//            ),
//            Parameter(
//                name = "size",
//                `in` = Parameter.Location.query,
//                schema = TypeDefinition.INT
//            )
//        )
//        get = GetInfo.builder {
//            summary("Get course by id")
//            description("Get course data for a specific course")
//            response {
//                responseCode(HttpStatusCode.OK)
//                responseType<Course>()
//                description("Course Representation")
//            }
//            canRespond {
//                responseType<ErrorResponse>()
//                responseCode(HttpStatusCode.NotFound)
//                description("Indicates that a course with this id does not exist")
//            }
//        }
//    }
}