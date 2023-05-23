plugins {
    id("common")
    application
}

dependencies {
    implementation(project(":modell"))

    implementation(libs.jackson.core)
    implementation(libs.jackson.datatype.jsr310)

    // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-slf4j/
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:${libs.versions.kotlinx.coroutines.slf4j.get()}")

    // POC - iverksett api
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.client.logging.jvm)
    implementation("com.github.navikt.dp-biblioteker:oauth2-klient:${libs.versions.dagpenger.biblioteker.get()}")

    implementation(libs.rapids.and.rivers)
    implementation(libs.konfig)
    implementation(libs.kotlin.logging)

    testImplementation(libs.ktor.client.mock)
    testImplementation("io.kotest:kotest-assertions-core-jvm:${libs.versions.kotest.get()}")

    testImplementation("io.mockk:mockk:${libs.versions.mockk.get()}")
}

application {
    mainClass.set("no.nav.dagpenger.vedtak.mediator.AppKt")
}
