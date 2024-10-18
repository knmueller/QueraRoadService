import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlinSerialization)
    application
}

group = "com.kyle.quera"
version = "1.0-SNAPSHOT"

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

// Use KSP Generated sources
sourceSets.main {
    java.srcDirs("build/generated/ksp/main/kotlin")
}

dependencies {
    implementation(projects.shared)

    implementation(project(":ktorServerCommon"))
    implementation(libs.logback)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.json)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.resources)

    // Dependency injection. Koin BOM version specified in `shared` lib
    implementation(libs.koin.annotations)
    implementation(libs.koin.logger)
    implementation(libs.koin.ktor)
    ksp(libs.koin.ksp.compiler)

    // Database
    val komapperVersion: String by project
    platform("org.komapper:komapper-platform:$komapperVersion").let {
        implementation(it)
        ksp(it)
    }
    implementation(libs.komapper.starter.r2dbc)
    implementation(libs.komapper.dialect.postgresql.r2dbc)
    implementation(libs.komapper.dialect.h2.r2dbc)
    implementation(libs.r2dbc.migrate)
    implementation(libs.r2dbc.migrate.impl)
    ksp(libs.komapper.processor)

    // Documentation TODO
//    implementation(files("../../kompendium/core/build/libs/kompendium-core-4.1.0-SNAPSHOT.jar"))
//    implementation(files("../../kompendium/enrichment/build/libs/kompendium-enrichment-4.1.0-SNAPSHOT.jar"))
//    implementation(files("../../kompendium/json-schema/build/libs/kompendium-json-schema-4.1.0-SNAPSHOT.jar"))
//    implementation(files("../../kompendium/oas/build/libs/kompendium-oas-4.1.0-SNAPSHOT.jar"))
//    implementation(files("../../kompendium/resources/build/libs/kompendium-resources-4.1.0-SNAPSHOT.jar"))
//    implementation("io.bkbn:kompendium-core:latest.release")
//    implementation("io.bkbn:kompendium-resources:4.0.0-alpha")
//    implementation("io.ktor:ktor-server-swagger")

    testImplementation(kotlin("test"))
    testImplementation(libs.mockk)
    testImplementation(libs.koin.test.junit5)
    testImplementation(libs.ktor.server.tests)
    testImplementation(libs.ktor.client.content.negotiation)
}

tasks.withType<KotlinCompile> { // Settings for `KotlinCompile` tasks
    compilerOptions {
        freeCompilerArgs.add("-opt-in=org.komapper.annotation.KomapperExperimentalAssociation")
        jvmTarget.set(JvmTarget.JVM_21)
    }
}