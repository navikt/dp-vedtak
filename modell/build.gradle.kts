plugins {
    id("common")
    `java-library`
}

val cucumberVersjon = "7.12.0"

dependencies {
    testImplementation("io.cucumber:cucumber-java8:$cucumberVersjon")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersjon")
    testImplementation("org.junit.platform:junit-platform-suite:1.9.3")
    testImplementation("io.kotest:kotest-assertions-core-jvm:${libs.versions.kotest.get()}")

    // Dette er målet:
    // testImplementation(libs.jackson.datatype)
    // testImplementation(libs.jackson.module)
    // Midlertidig løsning for å fjerne jsr310 og module kotlin fra buildSrc
    testImplementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${libs.versions.jackson.get()}")
    testImplementation("com.fasterxml.jackson.module:jackson-module-kotlin:${libs.versions.jackson.get()}")
}
