plugins {
    id("common")
    application
}

dependencies {
    implementation(project(path = ":modell"))
    implementation(project(path = ":openapi"))

    implementation(libs.jackson.core)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.blackbird)

    implementation(libs.bundles.postgres)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${libs.versions.kotlinx.coroutines.slf4j.get()}")

    implementation(libs.rapids.and.rivers)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)

    implementation(libs.bundles.ktor.client)
    implementation(libs.bundles.ktor.server)
    implementation("io.ktor:ktor-server-core-jvm:${libs.versions.ktor.get()}")
    implementation("io.ktor:ktor-server-swagger:${libs.versions.ktor.get()}")

    testImplementation(libs.ktor.client.mock)
    testImplementation("io.kotest:kotest-assertions-core-jvm:${libs.versions.kotest.get()}")

    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation(libs.mock.oauth2.server)
    testImplementation(libs.bundles.postgres.test)
    testImplementation(libs.ktor.server.test.host.jvm)
}

application {
    mainClass.set("no.nav.dagpenger.vedtak.mediator.AppKt")
}
