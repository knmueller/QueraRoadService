package com.kyle.quera.route

import com.kyle.quera.core.SignsService
import com.kyle.quera.model.Sign
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
@Resource("signs")
class SignsRequest(val roadId: Int? = null, val sort: String = "createdAt,desc", val page: Int = 0, val size: Int = 10) {
    @Serializable
    @Resource("{id}")
    class Id(val parent: SignsRequest = SignsRequest(), val id: Int)
}

fun Routing.configureSignRoutes() {
    val svc: SignsService by inject(SignsService::class.java)

//    signsDocumentation()
    // Get all signs
    get<SignsRequest> { sign ->
        println("INJECTED get ${svc::class.java}")

        val pagedSigns = svc.getResources(sign)
        call.respond(pagedSigns)
    }

    // Get a single sign
    get<SignsRequest.Id> { sign ->
        println("INJECTED get ${svc::class.java}")

        val foundSign = svc.getResource(sign.id)
        call.respond(foundSign)
    }

    // Create a new Sign
    post<SignsRequest> {
        val signBody = call.receive<Sign>()
        val newSign = svc.createResource(signBody)
        call.respond(HttpStatusCode.Companion.Created, newSign)
    }

    // Delete a sign
    delete<SignsRequest.Id> { sign ->
        svc.deleteResource(sign.id)
        call.respond(HttpStatusCode.NoContent)
    }
}

fun Route.signDocumentation() {
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