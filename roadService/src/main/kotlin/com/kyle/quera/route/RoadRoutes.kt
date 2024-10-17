package com.kyle.quera.route

import com.kyle.quera.core.RoadsService
import com.kyle.quera.model.Road
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
@Resource("roads")
class RoadsRequest(
    val intersectionId: Int? = null,
    val sort: String = "createdAt,desc",
    val page: Int = 0, val size: Int = 10
) {
    @Serializable
    @Resource("{id}")
    class Id(val parent: RoadsRequest = RoadsRequest(), val id: Int)
}

fun Routing.configureRoadRoutes() {
    val svc: RoadsService by inject(RoadsService::class.java)

    roadDocumentation()
    
    get<RoadsRequest> { road ->
        println("INJECTED get ${svc::class.java}")

        val pagedRoads = svc.getResources(road)
        call.respond(pagedRoads)
    }

    get<RoadsRequest.Id> { road ->
        println("INJECTED get ${svc::class.java}")

        val foundRoad = svc.getResource(road.id)
        call.respond(foundRoad)
    }

    post<RoadsRequest> {
        val roadBody = call.receive<Road>()
        val newRoad = svc.createResource(roadBody)
        call.respond(HttpStatusCode.Companion.Created, newRoad)
    }

    delete<RoadsRequest.Id> { road ->
        svc.deleteResource(road.id)
        call.respond(HttpStatusCode.NoContent)
    }
}

fun Route.roadDocumentation() {
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