package com.kyle.quera

import com.kyle.quera.config.AppModule
import com.kyle.quera.config.DatabaseImpl
import com.kyle.quera.core.RoadsService
import com.kyle.quera.db.common.DAOFacade
import com.kyle.quera.db.common.Database
import com.kyle.quera.db.common.PagingAndSorting
import com.kyle.quera.db.model.RoadTable
import com.kyle.quera.model.Road
import com.kyle.quera.model.PagedResponse
import com.kyle.quera.model.SurfaceType
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import io.mockk.mockkClass
import io.r2dbc.postgresql.util.LogLevel
import io.mockk.coEvery
import io.mockk.coVerify
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.ExperimentalSerializationApi
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ksp.generated.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.koin.test.junit5.KoinTestExtension
import org.koin.test.junit5.mock.MockProviderExtension
import org.koin.test.mock.declareMock
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Integration tests for the application module
 *
 */
class RoadTest : KoinTest {

    companion object {
        private const val apiBase = "/api/1"
        private const val roadBaseApi = "$apiBase/roads"
    }

    private val daoMock: RoadTable by inject()
    private lateinit var jsonClient: HttpClient

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single<Database> { DatabaseImpl(LogLevel.DEBUG) }
                singleOf(::RoadTable) { bind<DAOFacade<Road>>() }
                single { RoadsService() }
            })
    }

    @JvmField
    @RegisterExtension
    val mockProvider = MockProviderExtension.create { clazz ->
        mockkClass(clazz)
    }

    /**
     * Specifies a fixed Date for testing.
     */
    private val date = LocalDateTime(2010, 1, 1, 0, 0, 0)

    @Test
    fun mockProviderTest() = testApplication {

        declareMock<RoadTable> {
            coEvery { countResource() } returns 5
        }

        runBlocking {
            assertEquals(5, daoMock.countResource())
        }
    }

    /**
     * Tests the GET /roads/id response.
     * [DAOFacade] is mocked and verified [DAOFacade.getResource] is called once.
     */
    @Test
    fun testRoadsGetOne() = testApplication {
        setupApp()

        val expectedRoad = Road.generate(1).take(1).first()

        declareMock<RoadTable> {
            coEvery { daoMock.getResource(eq(1)) } returns expectedRoad
        }

        jsonClient.get("$roadBaseApi/1").apply {
            assertEquals(200, status.value)
            assertEquals(expectedRoad, body<Road>())
        }

        coVerify(exactly = 1) { daoMock.getResource(1) }
    }

    /**
     * Tests the GET /roads/id response with an unknown id.
     * [DAOFacade] is mocked and verified [DAOFacade.getResource] is called once.
     */
    @Test
    fun testRoadsGetOne404() = testApplication {
        setupApp()

        val expectedRoad = Road.generate(1).take(1).first()

        // todo throw exception
        declareMock<RoadTable> {
            coEvery { daoMock.getResource(eq(1)) } returns expectedRoad
        }

        jsonClient.get("$roadBaseApi/1").apply {
            assertEquals(200, status.value)
            assertEquals(expectedRoad, body<Road>())
        }

        coVerify(exactly = 1) { daoMock.getResource(1) }
    }

    /**
     * Tests the GET /roads api.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testRoadsGetAll() = testApplication {
        setupApp()

        val roadSize = 5
        val expectedRoad = Road.generate(3).take(roadSize).toList()

        declareMock<RoadTable> {
            coEvery { daoMock.getResources(PagingAndSorting()) } returns expectedRoad
            coEvery { daoMock.countResource() } returns expectedRoad.size.toLong()
        }

        jsonClient.get(roadBaseApi).apply {
            assertEquals(200, status.value)
            assertEquals(roadSize, body<PagedResponse<Road>>().size)
        }

        coVerify(exactly = 1) { daoMock.getResources(any()) }
    }

    /**
     * Tests the GET /roads api with paging.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testRoadsGetAllPaging() = testApplication {
        setupApp()

        val roadSize = 20
        val expectedRoad = Road.generate(3).take(roadSize).sortedBy { it.id }.toList()
        
        val page = 0
        val size = 5

        // mock getResources with [any], allowing the router/service to fill in the correct data
        declareMock<RoadTable> {
            coEvery { daoMock.getResources(any<PagingAndSorting>()) } returns expectedRoad.slice(
                (page * size)..<(page * size) + size
            )
            coEvery { daoMock.countResource() } returns expectedRoad.size.toLong()
        }

        jsonClient.get("$roadBaseApi?page=$page&size=$size").apply {
            assertEquals(200, status.value)
            val rsp = body<PagedResponse<Road>>()
            assertEquals(size, rsp.size)
            assertEquals(expectedRoad.size.toLong(), rsp.total)
        }

        // verify the router/svc set the correct paging and sorting params.
        coVerify(exactly = 1) {
            daoMock.getResources(
                eq(
                    PagingAndSorting(
                        page = page,
                        size = size
                    )
                )
            )
        }
    }

    /**
     * Tests the GET /roads api with paging.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testRoadsGetAllPagingMiddlePage() = testApplication {
        setupApp()

        val roadSize = 20
        val expectedRoad = Road.generate(3).take(roadSize).sortedBy { it.id }.toList()

        val page = 2
        val size = 5

        // mock getResources with [any], allowing the router/service to fill in the correct data
        declareMock<RoadTable> {
            coEvery { daoMock.getResources(any<PagingAndSorting>()) } returns expectedRoad.slice(
                (page * size)..<(page * size) + size
            )
            coEvery { daoMock.countResource() } returns expectedRoad.size.toLong()
        }

        jsonClient.get("$roadBaseApi?page=$page&size=$size").apply {
            assertEquals(200, status.value)
            val rsp = body<PagedResponse<Road>>()
            assertEquals(size, rsp.size)
            assertEquals(expectedRoad.size.toLong(), rsp.total)
        }

        // verify the router/svc set the correct paging and sorting params.
        coVerify(exactly = 1) {
            daoMock.getResources(
                eq(
                    PagingAndSorting(
                        page = page,
                        size = size
                    )
                )
            )
        }
    }

    /**
     * Tests the GET /roads api with sorting.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testRoadsGetAllDefaultSorting() = testApplication {
        setupApp()

        val roadSize = 20
        val expectedRoad =
            Road.generate(3).take(roadSize).sortedByDescending { it.createdAt }.toList()

        val page = 0
        val size = 10

        // mock getResources with [any], allowing the router/service to fill in the correct data
        declareMock<RoadTable> {
            coEvery { daoMock.getResources(any<PagingAndSorting>()) } returns expectedRoad.slice(
                (page * size)..<(page * size) + size
            )
            coEvery { daoMock.countResource() } returns expectedRoad.size.toLong()
        }

        jsonClient.get("$roadBaseApi?page=$page&size=$size").apply {
            assertEquals(200, status.value)
            val rsp = body<PagedResponse<Road>>()
            assertEquals(size, rsp.size)
            assertEquals(expectedRoad.size.toLong(), rsp.total)
            // validate order
            for (i in 1..<rsp.size) {
                assertTrue(rsp.elements[i - 1].createdAt!! > rsp.elements[i].createdAt!!)
            }
        }

        // verify the router/svc set the correct paging and sorting params.
        coVerify(exactly = 1) {
            daoMock.getResources(
                eq(
                    PagingAndSorting(
                        sort = "createdAt,desc",
                        page = page,
                        size = size
                    )
                )
            )
        }
    }

    /**
     * Tests the POST /roads api
     * [DAOFacade] is mocked and verified [DAOFacade.countResource] is called once.
     */
    @Test
    fun testRoadsPost() = testApplication {
        setupApp()

        val roadPostBody = Road(surfaceType = SurfaceType.gravel, intersectionId = 4)

        declareMock<RoadTable> {
            coEvery { daoMock.createResource(any<Road>()) } returns
                    Road(
                        1,
                        roadPostBody.surfaceType,
                        roadPostBody.intersectionId,
                        Clock.System.now().toLocalDateTime(TimeZone.UTC),
                        Clock.System.now().toLocalDateTime(TimeZone.UTC)
                    )
        }

        jsonClient.post(roadBaseApi) {
            contentType(ContentType.Application.Json)
            setBody(roadPostBody)
        }.apply {
            assertEquals(201, status.value)
            val body = body<Road>()
            assertEquals(roadPostBody.surfaceType, body.surfaceType)
            assertEquals(roadPostBody.intersectionId, body.intersectionId)
            assertNotNull(body.id)
            assertNotNull(body.createdAt)
            assertNotNull(body.updatedAt)
        }

        coVerify(exactly = 1) { daoMock.createResource(eq(roadPostBody)) }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun ApplicationTestBuilder.setupApp() {
        application {
            module()
            AppModule().module
        }
        // Use a json client for request/response
        jsonClient = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        environment {
            config = MapApplicationConfig()
        }
    }
}