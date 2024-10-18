package com.kyle.quera.config

import com.kyle.quera.db.common.DatabaseBaseImpl
import com.kyle.quera.db.common.R2DbImpl
import io.r2dbc.h2.H2ConnectionFactoryMetadata
import io.r2dbc.postgresql.util.LogLevel
import name.nkonev.r2dbc.migrate.core.R2dbcMigrate
import name.nkonev.r2dbc.migrate.core.R2dbcMigrateProperties
import name.nkonev.r2dbc.migrate.reader.ReflectionsClasspathResourceReader
import org.komapper.core.dsl.query.Query
import org.komapper.core.dsl.query.QueryScope
import org.komapper.dialect.h2.r2dbc.H2R2dbcDialect
import org.komapper.dialect.postgresql.r2dbc.PostgreSqlR2dbcDialect
import org.komapper.r2dbc.DefaultR2dbcDatabaseConfig
import org.komapper.r2dbc.R2dbcDatabase
import org.komapper.r2dbc.R2dbcDatabaseConfig
import org.slf4j.LoggerFactory


class R2DbKomapperImpl(database: R2dbcDatabase) : R2DbImpl<R2dbcDatabase>(database) {
    override suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T = r2DbImpl.runQuery(block)
//    override suspend fun runQuery(query: EntityStoreQuery): EntityStore = r2DbImpl.runQuery(query)
}

// Komapper specific wrapper to the DB. If another DB is being used, another implementation of
// this class should be created.
class DatabaseImpl(logLevel: LogLevel = LogLevel.WARN) : DatabaseBaseImpl(logLevel) {
    companion object {
        private var MIGRATED = false
        private val logger = LoggerFactory.getLogger(DatabaseBaseImpl::class.java)
    }
    override fun getDb(): R2DbImpl<*> {
        if (!MIGRATED) {
            logger.info("Starting database migration")

            val properties = R2dbcMigrateProperties()
            properties.setResourcesPath("db/migrations/")
            R2dbcMigrate.migrate(
                dataSource,
                properties,
                ReflectionsClasspathResourceReader(),
                null,
                null
            ).block()
            MIGRATED = true

            logger.info("Database Migrated successfully")
        }

        // with DataSource
        val config: R2dbcDatabaseConfig =
            when(dataSource.metadata.name) {
                H2ConnectionFactoryMetadata.NAME -> object : DefaultR2dbcDatabaseConfig(dataSource, H2R2dbcDialect()) {
                    // you can override properties here
                }
                // else assume postgres
                else -> object : DefaultR2dbcDatabaseConfig(dataSource, PostgreSqlR2dbcDialect()) {
                    // you can override properties here
                }
            }

        return R2DbKomapperImpl(R2dbcDatabase(config))
    }

    override suspend fun <T> runQuery(block: QueryScope.() -> Query<T>): T {
        return database.runQuery(block)
    }

//    override suspend fun runQuery(query: EntityStoreQuery): EntityStore {
//        return database.runQuery(query)
//    }
}