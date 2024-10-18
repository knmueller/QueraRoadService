package com.kyle.quera

import com.kyle.quera.config.AppModule
import com.kyle.quera.config.DatabaseImpl
import com.kyle.quera.core.SignsService
import com.kyle.quera.db.common.DAOFacade
import com.kyle.quera.db.common.Database
import com.kyle.quera.db.common.PagingAndSorting
import com.kyle.quera.db.model.SignTable
import com.kyle.quera.model.Sign
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
class SignTest : KoinTest {

    companion object {
        private const val apiBase = "/api/1"
        private const val signBaseApi = "$apiBase/signs"
    }

    private val daoMock: SignTable by inject()
    private lateinit var jsonClient: HttpClient

    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create {
        modules(
            module {
                single<Database> { DatabaseImpl(LogLevel.DEBUG) }
                singleOf(::SignTable) { bind<DAOFacade<Sign>>() }
                single { SignsService() }
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

        declareMock<SignTable> {
            coEvery { countResource() } returns 5
        }

        runBlocking {
            assertEquals(5, daoMock.countResource())
        }
    }

    /**
     * Tests the GET /signs/id response.
     * [DAOFacade] is mocked and verified [DAOFacade.getResource] is called once.
     */
    @Test
    fun testSignsGetOne() = testApplication {
        setupApp()

        val expectedSign = Sign.generate(1).take(1).first()

        declareMock<SignTable> {
            coEvery { daoMock.getResource(eq(1)) } returns expectedSign
        }

        jsonClient.get("$signBaseApi/1").apply {
            assertEquals(200, status.value)
            assertEquals(expectedSign, body<Sign>())
        }

        coVerify(exactly = 1) { daoMock.getResource(1) }
    }

    /**
     * Tests the GET /signs/id response with an unknown id.
     * [DAOFacade] is mocked and verified [DAOFacade.getResource] is called once.
     */
    @Test
    fun testSignsGetOne404() = testApplication {
        setupApp()

        val expectedSign = Sign.generate(1).take(1).first()

        declareMock<SignTable> {
            coEvery { daoMock.getResource(eq(1)) } returns expectedSign
        }

        jsonClient.get("$signBaseApi/1").apply {
            assertEquals(200, status.value)
            assertEquals(expectedSign, body<Sign>())
        }

        coVerify(exactly = 1) { daoMock.getResource(1) }
    }

    /**
     * Tests the GET /signs api.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testSignsGetAll() = testApplication {
        setupApp()

        val signSize = 5
        val expectedSign = Sign.generate(3).take(signSize).toList()

        declareMock<SignTable> {
            coEvery { daoMock.getResources(PagingAndSorting()) } returns expectedSign
            coEvery { daoMock.countResource() } returns expectedSign.size.toLong()
        }

        jsonClient.get(signBaseApi).apply {
            assertEquals(200, status.value)
            assertEquals(signSize, body<PagedResponse<Sign>>().size)
        }

        coVerify(exactly = 1) { daoMock.getResources(any()) }
    }

    /**
     * Tests the GET /signs api with paging.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testSignsGetAllPaging() = testApplication {
        setupApp()

        val signSize = 20
        val expectedSign = Sign.generate(3).take(signSize).sortedBy { it.id }.toList()
        
        val page = 0
        val size = 5

        // mock getResources with [any], allowing the router/service to fill in the correct data
        declareMock<SignTable> {
            coEvery { daoMock.getResources(any<PagingAndSorting>()) } returns expectedSign.slice(
                (page * size)..<(page * size) + size
            )
            coEvery { daoMock.countResource() } returns expectedSign.size.toLong()
        }

        jsonClient.get("$signBaseApi?page=$page&size=$size").apply {
            assertEquals(200, status.value)
            val rsp = body<PagedResponse<Sign>>()
            assertEquals(size, rsp.size)
            assertEquals(expectedSign.size.toLong(), rsp.total)
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
     * Tests the GET /signs api with paging.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testSignsGetAllPagingMiddlePage() = testApplication {
        setupApp()

        val signSize = 20
        val expectedSign = Sign.generate(3).take(signSize).sortedBy { it.id }.toList()

        val page = 2
        val size = 5

        // mock getResources with [any], allowing the router/service to fill in the correct data
        declareMock<SignTable> {
            coEvery { daoMock.getResources(any<PagingAndSorting>()) } returns expectedSign.slice(
                (page * size)..<(page * size) + size
            )
            coEvery { daoMock.countResource() } returns expectedSign.size.toLong()
        }

        jsonClient.get("$signBaseApi?page=$page&size=$size").apply {
            assertEquals(200, status.value)
            val rsp = body<PagedResponse<Sign>>()
            assertEquals(size, rsp.size)
            assertEquals(expectedSign.size.toLong(), rsp.total)
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
     * Tests the GET /signs api with sorting.
     * [DAOFacade] is mocked and verified [DAOFacade.getResources] is called once.
     */
    @Test
    fun testSignsGetAllDefaultSorting() = testApplication {
        setupApp()

        val signSize = 20
        val expectedSign =
            Sign.generate(3).take(signSize).sortedByDescending { it.createdAt }.toList()

        val page = 0
        val size = 10

        // mock getResources with [any], allowing the router/service to fill in the correct data
        declareMock<SignTable> {
            coEvery { daoMock.getResources(any<PagingAndSorting>()) } returns expectedSign.slice(
                (page * size)..<(page * size) + size
            )
            coEvery { daoMock.countResource() } returns expectedSign.size.toLong()
        }

        jsonClient.get("$signBaseApi?page=$page&size=$size").apply {
            assertEquals(200, status.value)
            val rsp = body<PagedResponse<Sign>>()
            assertEquals(size, rsp.size)
            assertEquals(expectedSign.size.toLong(), rsp.total)
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
     * Tests the POST /signs api
     * [DAOFacade] is mocked and verified [DAOFacade.countResource] is called once.
     */
    @Test
    fun testSignsPost() = testApplication {
        setupApp()

        val signPostBody = Sign(roadId = 4)

        declareMock<SignTable> {
            coEvery { daoMock.createResource(any<Sign>()) } returns
                    Sign(
                        1,
                        signPostBody.roadId,
                        Clock.System.now().toLocalDateTime(TimeZone.UTC),
                        Clock.System.now().toLocalDateTime(TimeZone.UTC)
                    )
        }

        jsonClient.post(signBaseApi) {
            contentType(ContentType.Application.Json)
            setBody(signPostBody)
        }.apply {
            assertEquals(201, status.value)
            val body = body<Sign>()
            assertEquals(signPostBody.roadId, body.roadId)
            assertNotNull(body.id)
            assertNotNull(body.createdAt)
            assertNotNull(body.updatedAt)
        }

        coVerify(exactly = 1) { daoMock.createResource(eq(signPostBody)) }
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