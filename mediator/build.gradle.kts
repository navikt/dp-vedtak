plugins {
    id("common")
    application
}

val githubUser: String? by project
val githubPassword: String? by project

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/navikt/dp-kontrakter")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
}

dependencies {
    implementation(project(path = ":modell"))
    implementation(project(path = ":aktivitetslogg"))

    // Kontrakter for dp-iverksett
    implementation("no.nav.dagpenger.kontrakter:iverksett:2.0_20230613092537_6f4e0f0")

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
