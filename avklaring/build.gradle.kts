
plugins {
    id("common")
    `java-library`
}
dependencies {
    implementation(project(path = ":opplysninger"))
    implementation(project(path = ":dato"))

    testImplementation(libs.kotest.assertions.core)
}
