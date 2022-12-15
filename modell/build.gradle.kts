plugins {
    id("dagpenger.common")
}

val cucumberVersjon = "7.10.0"
dependencies {
    testImplementation("io.cucumber:cucumber-java8:$cucumberVersjon")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersjon")
    testImplementation("org.junit.platform:junit-platform-suite:1.9.1")

    testImplementation(Jackson.kotlin)
    testImplementation(Jackson.jsr310)
}
