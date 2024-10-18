package com.kyle.quera.db.common

import com.common.ktor.server.util.getPropertyOrThrow
import com.kyle.quera.config.DatabaseImpl
import com.kyle.quera.db.model.IntersectionTable
import com.kyle.quera.db.model.RoadTable
import com.kyle.quera.db.model.SignTable
import com.kyle.quera.model.Intersection
import com.kyle.quera.model.Road
import com.kyle.quera.model.Sign
import io.ktor.server.application.ApplicationEnvironment
import io.ktor.server.plugins.BadRequestException
import io.r2dbc.h2.H2ConnectionConfiguration
import io.r2dbc.h2.H2ConnectionFactory
import io.r2dbc.h2.H2ConnectionOption
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration
import io.r2dbc.postgresql.PostgresqlConnectionFactory
import io.r2dbc.postgresql.util.LogLevel
import io.r2dbc.spi.ConnectionFactory
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.komapper.core.dsl.expression.SortExpression
import org.komapper.core.dsl.operator.asc
import org.komapper.core.dsl.operator.desc
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.slf4j.LoggerFactory

@ExperimentalSerializationApi
fun databaseModule(environment: ApplicationEnvironment) =
    module {
        println("app config = ${environment.config}")
        val logLevel = LogLevel.DEBUG
        single<Database> { DatabaseImpl(logLevel) }
        singleOf(::IntersectionTable) { bind<DAOFacade<Intersection>>() }
        singleOf(::RoadTable) { bind<DAOFacade<Road>>() }
        singleOf(::SignTable) { bind<DAOFacade<Sign>>() }
    }

interface Database {
    suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T
}

abstract class DatabaseBaseImpl(logLevel: LogLevel = LogLevel.WARN, cleanDB: Boolean = false) :
    Database, KoinComponent {

    private val logger = LoggerFactory.getLogger(DatabaseBaseImpl::class.java)

    private val databaseType = getPropertyOrThrow("ktor.database.type") // postgres || h2
    private val databaseName = getPropertyOrThrow("ktor.database.name")
    private val host = getPropertyOrThrow("ktor.database.host")
    private val user = getPropertyOrThrow("ktor.database.user")
    private val password = getPropertyOrThrow("ktor.database.password")

    abstract fun getDb(): R2DbImpl<*>

    protected val dataSource: ConnectionFactory by lazy {
        when (databaseType) {
            "postgres" -> {
                val postgresOptions = PostgresqlConnectionConfiguration.builder()
                    .host(host)
                    .username(user)
                    .password(password)
                    .database(databaseName)
                    .build()

                PostgresqlConnectionFactory(postgresOptions)
            }

            "h2" -> {
                H2ConnectionFactory(
                    H2ConnectionConfiguration.builder()
                        .inMemory(databaseName)
                        .option("${H2ConnectionOption.DB_CLOSE_DELAY}=-1")
                        .build()
                )
            }

            else -> {
                throw RuntimeException("Unsupported Database type: $databaseType")
            }
        }
    }

    protected val database by lazy {
        logger.debug("database databaseName: $databaseName")
        logger.debug("database host:         $host")
        logger.debug("database user:         $user")
        logger.debug("database password:     ***")

        getDb()
    }
}

abstract class R2DbImpl<T>(val r2DbImpl: T) {
    abstract suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T
}

data class PagingAndSorting(
    val sort: String = "createdAt,desc",
    val page: Int = 0,
    val size: Int = 10
)

abstract class EntityWrapper<T : `org.komapper.core.dsl.metamodel`.EntityMetamodel<*, *, *>, E>(
    protected val entity: T
) : KoinComponent, DAOFacade<E> {
    protected val database by inject<Database>()
    protected abstract suspend fun count(): Long
    protected abstract suspend fun create(resource: E): E
    protected abstract suspend fun delete(id: Int)
    protected abstract suspend fun find(id: Int): E
    protected abstract suspend fun findAll(pas: PagingAndSorting): List<E>
    protected abstract suspend fun findAll(pas: PagingAndSorting, block: QueryScope.() -> Query<List<E>>): List<E>

    override suspend fun countResource(): Long = count()
    override suspend fun createResource(resource: E): E = create(resource)
    override suspend fun deleteResource(id: Int) = delete(id)
    override suspend fun getResource(id: Int): E = find(id)
    override suspend fun getResources(pagingAndSorting: PagingAndSorting): List<E> =
        findAll(pagingAndSorting)
    override suspend fun getResources(pagingAndSorting: PagingAndSorting, block: QueryScope.() -> Query<List<E>>): List<E> = findAll(pagingAndSorting, block)

    fun toOrderBy(pas: PagingAndSorting): List<SortExpression> {
        val sortParam = pas.sort.split(",")
        if (sortParam.size != 2) {
            throw BadRequestException("Invalid sort param")
        }

        val prop = entity.properties().find { it.name == sortParam[0] }
        if (prop == null) {
            throw BadRequestException("Invalid sort param: ${sortParam[0]}")
        }

        // sort on id 2nd.
        val idProp = entity.properties().find { it.name == "id" }

        return when (sortParam[1].trim()) {
            "asc" -> {
                listOfNotNull(prop.asc(), idProp?.asc())
            }

            "desc" -> {
                listOfNotNull(prop.desc(), idProp?.desc())
            }

            else -> throw BadRequestException("Invalid sort param")
        }
    }
}