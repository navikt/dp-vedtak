plugins {
    id("common")
    `java-library`
}
val cucumberVersion = "7.15.0"

dependencies {
    implementation(project(path = ":opplysninger"))
    implementation(project(path = ":dato"))
    implementation("com.github.navikt:dp-grunnbelop:2023.05.24-15.26.f42064d9fdc8")

    testImplementation("io.cucumber:cucumber-java8:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation("org.junit.platform:junit-platform-suite:1.10.2")
    testImplementation("com.approvaltests:approvaltests:22.3.3")
}
