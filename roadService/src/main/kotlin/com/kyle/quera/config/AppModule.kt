package com.kyle.quera.config

import com.kyle.quera.db.common.databaseModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import kotlinx.serialization.ExperimentalSerializationApi
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.ksp.generated.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger


// Scan this package for Koin components
@Module
@ComponentScan("com.kyle.quera")
class AppModule

// Add the scanned components and the database environment.
@ExperimentalSerializationApi
fun Application.configureKoin() {
    install(Koin) {
        slf4jLogger()
        modules(
            AppModule().module,
            databaseModule(environment)
        )
    }
}
