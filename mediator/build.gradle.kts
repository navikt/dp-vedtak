plugins {
    id("dagpenger.common")
    id("dagpenger.rapid-and-rivers")
}

dependencies {
    implementation(project(":modell"))

    implementation(Jackson.core)
    implementation(Jackson.jsr310)

    // https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-slf4j/
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.6.4")

    // POC - iverksett api
    implementation("io.ktor:ktor-client-core:${Ktor2.version}")
    implementation("io.ktor:ktor-client-cio:${Ktor2.version}")
    implementation("io.ktor:ktor-client-content-negotiation:${Ktor2.version}")
    implementation("io.ktor:ktor-serialization-jackson:${Ktor2.version}")
    implementation("io.ktor:ktor-client-logging:${Ktor2.version}")
    implementation("com.github.navikt.dp-biblioteker:oauth2-klient:${Dagpenger.Biblioteker.version}")

    testImplementation("io.ktor:ktor-client-mock:${Ktor2.version}")

    testImplementation(Mockk.mockk)
}
application {
    mainClass.set("no.nav.dagpenger.vedtak.mediator.AppKt")
}
