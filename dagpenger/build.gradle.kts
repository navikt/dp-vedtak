plugins {
    id("common")
    `java-library`
}
val cucumberVersion = "7.15.0"

dependencies {
    implementation(project(path = ":opplysninger"))
    implementation(project(path = ":dato"))
    implementation(project(path = ":konklusjon"))
    implementation(project(path = ":avklaring"))
    implementation("com.github.navikt:dp-grunnbelop:2024.05.30-13.38.6e9169eb05d1")

    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java8:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation("org.junit.platform:junit-platform-suite:1.10.2")
    testImplementation("com.approvaltests:approvaltests:22.3.3")
    testImplementation(libs.bundles.jackson)
    testImplementation(libs.kotest.assertions.core)
}
