import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.21"
    maven
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))
    implementation(junit5("jupiter","5.6.0"))
    implementation(junit5("jupiter-params","5.6.0"))
}

repositories {
    jcenter()
}

group = "berlin.preussler.junit5"

tasks.withType<Test> {
    useJUnitPlatform()
}

// config JVM target to 1.8 for kotlin compilation tasks
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions.jvmTarget = "1.8"
}

fun DependencyHandler.junit5(module: String, version: String? = null): Any =
    "org.junit.jupiter:junit-$module${version?.let { ":$version" } ?: ""}"

apply(from= "publishing.gradle")
