plugins {
    id("common")
    application
}

dependencies {
    implementation(project(path = ":modell"))
    implementation(project(path = ":aktivitetslogg"))

    implementation(libs.jackson.core)
    implementation(libs.jackson.datatype.jsr310)
    implementation(libs.jackson.blackbird)

    // Database avhengighetere
    implementation(libs.bundles.postgres)

    // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-slf4j/
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${libs.versions.kotlinx.coroutines.slf4j.get()}")

    implementation(libs.rapids.and.rivers)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)

    testImplementation(libs.ktor.client.mock)
    testImplementation("io.kotest:kotest-assertions-core-jvm:${libs.versions.kotest.get()}")

    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
    testImplementation(libs.bundles.postgres.test)
}

application {
    mainClass.set("no.nav.dagpenger.vedtak.mediator.AppKt")
}
