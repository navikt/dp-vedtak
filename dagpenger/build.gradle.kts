plugins {
    id("common")
    `java-library`
}
val cucumberVersion = "7.15.0"

dependencies {
    implementation(project(path = ":dag"))
    implementation(project(path = ":opplysninger"))
    implementation(project(path = ":dato"))
    implementation(project(path = ":avklaring"))
    implementation("no.nav.dagpenger:dp-grunnbelop:20240821.109.11cc88")

    testImplementation("io.cucumber:cucumber-java:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-java8:$cucumberVersion")
    testImplementation("io.cucumber:cucumber-junit-platform-engine:$cucumberVersion")
    testImplementation("org.junit.platform:junit-platform-suite:1.10.2")
    testImplementation("com.approvaltests:approvaltests:22.3.3")
    testImplementation(libs.bundles.jackson)
    testImplementation(libs.kotest.assertions.core)
}
