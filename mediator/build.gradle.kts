plugins {
    id("common")
    application
}

dependencies {
    implementation(project(path = ":modell"))
    implementation(project(path = ":openapi"))

    implementation(libs.bundles.jackson)
    implementation("com.fasterxml.jackson.module:jackson-module-blackbird:${libs.versions.jackson.get()}")

    implementation(libs.bundles.postgres)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.7.3")

    implementation(libs.rapids.and.rivers)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)

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
    mainClass.set("no.nav.dagpenger.vedtak.mediator.AppKt")
}

tasks.withType<Jar>().configureEach {
    dependsOn(":modell:jar", ":openapi:jar")
    manifest { attributes["Main-Class"] = application.mainClass }
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}
