package com.kyle.quera

import com.kyle.quera.config.AppModule
import com.kyle.quera.config.DatabaseImpl
import com.kyle.quera.core.IntersectionsService
import com.kyle.quera.db.common.DAOFacade
import com.kyle.quera.db.common.Database
import com.kyle.quera.db.common.PagingAndSorting
import com.kyle.quera.db.model.IntersectionTable
import com.kyle.quera.model.Intersection
import com.kyle.quera.model.PagedResponse
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
import java.time.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDateTime
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
class IntersectionTest : KoinTest {

    companion object {
        private const val apiBase = "/api/1"
        private const val intersectionBaseApi = "$apiBase/intersections"
    }

    private val daoMock: IntersectionTable by inject()
    private lateinit var jsonClient: HttpClient

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single<Database> { DatabaseImpl(LogLevel.DEBUG) }
                singleOf(::IntersectionTable) { bind<DAOFacade<Intersection>>() }
                single { IntersectionsService() }
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
    private val date = kotlinx.datetime.LocalDateTime(2010, 1, 1, 0, 0, 0).toJavaLocalDateTime()

    @Test
    fun mockProviderTest() = testApplication {

        declareMock<IntersectionTable> {
            coEvery { countResource() } returns 5
        }

        runBlocking {
            assertEquals(5, daoMock.countResource())
        }
    }

    /**
     * Tests the GET /intersections/id response.
     * [DAOFacade] is mocked and verified [DAOFacade.getResource] is called once.
     */
    @Test
    fun testIntersectionsGetOne() = testApplication {
        setupApp()

        val expectedIntersection = Intersection.generate().take(1).first()

        declareMock<IntersectionTable> {
            coEvery { daoMock.getResource(eq(1)) } returns expectedIntersection
        }

        jsonClient.get("$intersectionBaseApi/1").apply {
            assertEquals(200, status.value)
            assertEquals(expectedIntersection, body<Intersection>())
        }

        coVerify(exactly = 1) { daoMock.getResource(1) }
    }

    /**
     * Tests the GET /intersections/id response with an unknown id.
     * [DAOFacade] is mocked and verified [DAOFacade.getResource] is called once.
     */
    @Test
    fun testIntersectionsGetOne404() = testApplication {
        setupApp()

        val expectedIntersection = Intersection.generate().take(1).first()

        declareMock<IntersectionTable> {
            coEvery { daoMock.getResource(eq(1)) } returns expectedIntersection
        }

        jsonClient.get("$intersectionBaseApi/1").apply {
            assertEquals(200, status.value)
            assertEquals(expectedIntersection, body<Intersection>())
        }

        coVerify(exactly = 1) { daoMock.getResource(1) }
    }

    /**
     * Tests the GET /intersections api.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testIntersectionsGetAll() = testApplication {
        setupApp()

        val intersectionSize = 5
        val expectedIntersection = Intersection.generate().take(intersectionSize).toList()

        declareMock<IntersectionTable> {
            coEvery { daoMock.getResources(PagingAndSorting()) } returns expectedIntersection
            coEvery { daoMock.countResource() } returns expectedIntersection.size.toLong()
        }

        jsonClient.get(intersectionBaseApi).apply {
            assertEquals(200, status.value)
            assertEquals(intersectionSize, body<PagedResponse<Intersection>>().size)
        }

        coVerify(exactly = 1) { daoMock.getResources(any()) }
    }

    /**
     * Tests the GET /intersections api with paging.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testIntersectionsGetAllPaging() = testApplication {
        setupApp()

        val intersectionSize = 20
        val expectedIntersection = Intersection.generate().take(intersectionSize).sortedBy { it.id }.toList()
        
        val page = 0
        val size = 5

        // mock getResources with [any], allowing the router/service to fill in the correct data
        declareMock<IntersectionTable> {
            coEvery { daoMock.getResources(any<PagingAndSorting>()) } returns expectedIntersection.slice(
                (page * size)..<(page * size) + size
            )
            coEvery { daoMock.countResource() } returns expectedIntersection.size.toLong()
        }

        jsonClient.get("$intersectionBaseApi?page=$page&size=$size").apply {
            assertEquals(200, status.value)
            val rsp = body<PagedResponse<Intersection>>()
            assertEquals(size, rsp.size)
            assertEquals(expectedIntersection.size.toLong(), rsp.total)
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
     * Tests the GET /intersections api with paging.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testIntersectionsGetAllPagingMiddlePage() = testApplication {
        setupApp()

        val intersectionSize = 20
        val expectedIntersection = Intersection.generate().take(intersectionSize).sortedBy { it.id }.toList()

        val page = 2
        val size = 5

        // mock getResources with [any], allowing the router/service to fill in the correct data
        declareMock<IntersectionTable> {
            coEvery { daoMock.getResources(any<PagingAndSorting>()) } returns expectedIntersection.slice(
                (page * size)..<(page * size) + size
            )
            coEvery { daoMock.countResource() } returns expectedIntersection.size.toLong()
        }

        jsonClient.get("$intersectionBaseApi?page=$page&size=$size").apply {
            assertEquals(200, status.value)
            val rsp = body<PagedResponse<Intersection>>()
            assertEquals(size, rsp.size)
            assertEquals(expectedIntersection.size.toLong(), rsp.total)
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
     * Tests the GET /intersections api with sorting.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testIntersectionsGetAllDefaultSorting() = testApplication {
        setupApp()

        val intersectionSize = 20
        val expectedIntersection =
            Intersection.generate().take(intersectionSize).sortedByDescending { it.createdAt }.toList()

        val page = 0
        val size = 10

        // mock getResources with [any], allowing the router/service to fill in the correct data
        declareMock<IntersectionTable> {
            coEvery { daoMock.getResources(any<PagingAndSorting>()) } returns expectedIntersection.slice(
                (page * size)..<(page * size) + size
            )
            coEvery { daoMock.countResource() } returns expectedIntersection.size.toLong()
        }

        jsonClient.get("$intersectionBaseApi?page=$page&size=$size").apply {
            assertEquals(200, status.value)
            val rsp = body<PagedResponse<Intersection>>()
            assertEquals(size, rsp.size)
            assertEquals(expectedIntersection.size.toLong(), rsp.total)
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
     * Tests the POST /intersections api
     * [DAOFacade] is mocked and verified [DAOFacade.countResource] is called once.
     */
    @Test
    fun testIntersectionsPost() = testApplication {
        setupApp()

        val intersectionPostBody = Intersection(name = "4way")

        declareMock<IntersectionTable> {
            coEvery { daoMock.createResource(any<Intersection>()) } returns
                    Intersection(
                        1,
                        intersectionPostBody.name,
                        Clock.System.now().toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime(),
                        Clock.System.now().toLocalDateTime(TimeZone.UTC).toJavaLocalDateTime()
                    )
        }

        jsonClient.post(intersectionBaseApi) {
            contentType(ContentType.Application.Json)
            setBody(intersectionPostBody)
        }.apply {
            assertEquals(201, status.value)
            val body = body<Intersection>()
            assertEquals(intersectionPostBody.name, body.name)
            assertNotNull(body.id)
            assertNotNull(body.createdAt)
            assertNotNull(body.updatedAt)
        }

        coVerify(exactly = 1) { daoMock.createResource(eq(intersectionPostBody)) }
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