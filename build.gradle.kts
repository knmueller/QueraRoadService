import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}

allprojects {
    tasks.withType<KotlinCompile> { // Settings for `KotlinCompile` tasks
        compilerOptions {
            freeCompilerArgs.add("-Xjsr305=strict") // `-Xjsr305=strict` enables the strict mode for JSR-305 annotations
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}