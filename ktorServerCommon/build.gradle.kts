import org.gradle.api.execution.TaskExecutionGraph


plugins {
    `java-library`
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

group = "com.common.ktor.server"
version = "1.0.0"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

dependencies {
    implementation(projects.shared)

    implementation(libs.logback)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.cn)
    implementation(libs.ktor.server.json)
    implementation(libs.ktor.status.pages)

    implementation(libs.koin.annotations)
    implementation(libs.koin.logger)
    implementation(libs.koin.ktor)
    ksp(libs.koin.ksp.compiler)

    testImplementation(kotlin("test"))
    testImplementation(libs.ktor.server.tests)
}

// don't need the shadowJar (yet?). Disable it for now.
tasks.withType<Jar> {
    if (this.name.startsWith("shadow")) {
        println("Disabling ${this.name}")
        enabled = false
    }
}

//gradle.taskGraph.whenReady {
//
//    println("Found task graph: $this")
//    println("Found " + this.allTasks.size + " tasks.")
//    this.allTasks.forEach { task ->
//        println()
//        println("----- " + task + " -----")
//        println("depends on tasks: " + task.dependsOn)
//        println("inputs: ")
//        task.inputs.files.files.forEach { f -> println(" - $f")}
//        println("outputs: ")
//        task.outputs.files.files.forEach { f -> println(" + $f")}
//    }
//}