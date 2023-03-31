plugins {
    id("dagpenger.common")
    id("dagpenger.rapid-and-rivers")
}

dependencies {
    implementation(project(":modell"))

    testImplementation(Mockk.mockk)
}
application {
    mainClass.set("no.nav.dagpenger.vedtak.mediator.AppKt")
}
