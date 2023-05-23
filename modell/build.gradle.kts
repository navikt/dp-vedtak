plugins {
    id("dagpenger.common")
}

val cucumberVersjon = "7.12.0"
dependencies {
    implementation(project(":aktivitetslogg"))
    testImplementation("io.cucumber:cucumber-java8:$cucumberVersjon")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersjon")
    testImplementation("org.junit.platform:junit-platform-suite:1.9.3")

    testImplementation(Jackson.kotlin)
    testImplementation(Jackson.jsr310)
}
