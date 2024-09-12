import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("common")
    id("com.github.johnrengelman.shadow") version "8.1.1"
    application
    kotlin("plugin.serialization") version "2.0.20"
}

dependencies {
    implementation(project(path = ":konfigurasjon"))
    implementation(project(path = ":modell"))
    implementation(project(path = ":openapi"))
    implementation(project(path = ":dagpenger"))
    implementation(project(path = ":opplysninger"))
    implementation(project(path = ":avklaring"))
    implementation(project(path = ":uuid-v7"))

    implementation(libs.bundles.jackson)
    implementation("com.fasterxml.jackson.module:jackson-module-blackbird:${libs.versions.jackson.get()}")

    implementation(libs.bundles.postgres)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.7.3")

    implementation(libs.rapids.and.rivers)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)
    implementation("io.opentelemetry.instrumentation:opentelemetry-instrumentation-annotations:2.1.0")
    implementation("io.opentelemetry:opentelemetry-api:1.36.0")

    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.ktor.server)
    implementation("io.ktor:ktor-server-core-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-swagger:${libs.versions.ktor.get()}")

    testImplementation("io.kotest:kotest-assertions-core-jvm:${libs.versions.kotest.get()}")

    testImplementation(libs.mockk)
    testImplementation(libs.mock.oauth2.server)
    testImplementation(libs.bundles.postgres.test)
    testImplementation("io.ktor:ktor-server-test-host-jvm:${libs.versions.ktor.get()}")
}

application {
    mainClass.set("no.nav.dagpenger.behandling.mediator.AppKt")
}

tasks.withType<ShadowJar> {
    mergeServiceFiles()
}
