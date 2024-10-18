import com.google.devtools.ksp.gradle.KspTaskJS

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.ksp)
}

kotlin {
    jvm()
    
    sourceSets {
        val commonMain by getting {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
            dependencies {
                // put your Multiplatform dependencies here

                // Dependency injection
                api(project.dependencies.platform(libs.koin.bom))
                implementation(libs.koin.core)
                implementation(libs.koin.annotations)

                implementation(libs.kotlinx.coroutines.core)
                api(libs.kotlinx.datetime)
                implementation(libs.kotlinx.serialize.json)
            }
        }
    }
}

dependencies {
    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
    add("kspJvm", libs.koin.ksp.compiler)
}

// Fix ksp dependencies
//tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
//    if (name != "kspCommonMainKotlinMetadata") {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//}
//
//// Fix ksp dependencies
//tasks.withType<KspTaskJS>().all {
//    dependsOn("kspCommonMainKotlinMetadata")
//}
